/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.task.datax;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.task.datax.entity.ColumnInfo;
import org.apache.dolphinscheduler.plugin.task.datax.entity.DataxParameters;
import org.apache.dolphinscheduler.plugin.task.datax.entity.ElasticSearchDataxParams;
import org.apache.dolphinscheduler.plugin.task.datax.entity.HiveMetadata;
import org.apache.dolphinscheduler.plugin.task.datax.enums.AliasMode;
import org.apache.dolphinscheduler.plugin.task.datax.enums.WriteMode;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.enums.Flag;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils.decodePassword;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RWXR_XR_X;

public class DataxTask extends AbstractTaskExecutor {
    /**
     * jvm parameters
     */
    public static final String JVM_PARAM = " --jvm=\"-Xms%sG -Xmx%sG\" ";

    /**
     * datax path
     */
    private static final String DATAX_PATH = "${DATAX_HOME}/bin/datax.py";
    /**
     * datax channel count
     */
    private static final int DATAX_CHANNEL_COUNT = 1;

    /**
     * datax parameters
     */
    private DataxParameters dataXParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    private DataxTaskExecutionContext dataxTaskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public DataxTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;

        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext, logger);
    }

    /**
     * init DataX config
     */
    @Override
    public void init() {
        logger.info("datax task params {}", taskExecutionContext.getTaskParams());
        dataXParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), DataxParameters.class);

        if (!dataXParameters.checkParameters()) {
            throw new RuntimeException("datax task params is not valid");
        }

        dataxTaskExecutionContext = dataXParameters.generateExtendedContext(taskExecutionContext.getResourceParametersHelper());
    }

    /**
     * run DataX process
     *
     * @throws Exception if error throws Exception
     */
    @Override
    public void handle() throws Exception {
        try {
            // replace placeholder,and combine local and global parameters
            Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext, getParameters());
            if (MapUtils.isEmpty(paramsMap)) {
                paramsMap = new HashMap<>();
            }
            if (MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
                paramsMap.putAll(taskExecutionContext.getParamsMap());
            }

            // run datax procesDataSourceService.s
            String jsonFilePath = buildDataxJsonFile(paramsMap);
            String shellCommandFilePath = buildShellCommandFile(jsonFilePath, paramsMap);
            TaskResponse commandExecuteResult = shellCommandExecutor.run(shellCommandFilePath);

            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setAppIds(commandExecuteResult.getAppIds());
            setProcessId(commandExecuteResult.getProcessId());
        } catch (Exception e) {
            logger.error("datax task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw e;
        }
    }

    /**
     * cancel DataX process
     *
     * @param cancelApplication cancelApplication
     * @throws Exception if error throws Exception
     */
    @Override
    public void cancelApplication(boolean cancelApplication)
            throws Exception {
        // cancel process
        shellCommandExecutor.cancelApplication();
    }

    /**
     * build datax configuration file
     *
     * @return datax json file name
     * @throws Exception if error throws Exception
     */
    private String buildDataxJsonFile(Map<String, Property> paramsMap)
            throws Exception {
        // generate json
        String fileName = String.format("%s/%s_job.json",
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId());
        String json;

        Path path = new File(fileName).toPath();
        if (Files.exists(path)) {
            return fileName;
        }

        if (dataXParameters.getCustomConfig() == Flag.YES.ordinal()) {
            json = dataXParameters.getJson().replaceAll("\\r\\n", "\n");
        } else {
            ObjectNode job = JSONUtils.createObjectNode();
            job.putArray("content").addAll(buildDataxJobContentJsonFromConfiguration());

            job.set("setting", buildDataxJobSettingJson());

            ObjectNode root = JSONUtils.createObjectNode();
            root.set("job", job);
            root.set("core", buildDataxCoreJson());
            json = root.toString();
        }

        // replace placeholder
        json = ParameterUtils.convertParameterPlaceholders(json, ParamUtils.convert(paramsMap));

        logger.debug("datax job json : {}", json);

        // create datax json file
        FileUtils.writeStringToFile(new File(fileName), json, StandardCharsets.UTF_8);
        return fileName;
    }

    private List<ObjectNode> buildDataxJobContentJsonFromConfiguration() {
        List<ObjectNode> contentList = new ArrayList<>();
        ObjectNode content = JSONUtils.createObjectNode();
        content.set("reader", buildDataxReaderJson());
        content.set("writer", buildDataxWriterJson());
        contentList.add(content);
        return contentList;
    }

    public ObjectNode buildDataxReaderJson() {
        BaseConnectionParam dataSourceCfg = (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                dataxTaskExecutionContext.getSourcetype(),
                dataxTaskExecutionContext.getSourceConnectionParams());
        List<ObjectNode> readerConnArr = new ArrayList<>();

        ObjectNode readerParam = JSONUtils.createObjectNode();

        if (dataxTaskExecutionContext.getSourcetype().isHive()) {
            HiveMetadata hiveMetadata = tryGetHiveTableMetadataFromDDL(dataSourceCfg, dataXParameters.getSourceTable());
            ArrayNode columnArr = readerParam.putArray("column");
            if (CollectionUtils.isEmpty(dataXParameters.getDsColumns())) {
                columnArr.add("*");
            } else {
                for (ColumnInfo columnInfo : dataXParameters.getDsColumns()) {
                    if (columnInfo.isEnable()) {
                        columnArr.add(getHiveSourceColumnNodeFromColumnInfo(columnInfo));
                    }
                }
            }
            readerParam.put("path", getHdfsFilePath(hiveMetadata.getPath(), true));
            readerParam.put("defaultFS", hiveMetadata.getDefaultFS());
            readerParam.put("fileType", hiveMetadata.getFileType());
            readerParam.put("fieldDelimiter", hiveMetadata.getFieldDelimiter());
        } else {
            ObjectNode readerConn = JSONUtils.createObjectNode();
            ArrayNode tableArr = readerConn.putArray("table");
            boolean needAddDoubleQuotationMarks = needAddDoubleQuotationMarks(dataxTaskExecutionContext.getSourcetype());
            for (String table : new String[]{dataXParameters.getSourceTable()}) {
                if (needAddDoubleQuotationMarks){
                    tableArr.add(enclosesStrInQuotationMarks(table));
                }else {
                    tableArr.add(table);
                }
            }

            ArrayNode urlArr = readerConn.putArray("jdbcUrl");
            urlArr.add(DataSourceUtils.getJdbcUrl(DbType.valueOf(dataXParameters.getDsType()), dataSourceCfg));

            readerConnArr.add(readerConn);

            readerParam.put("username", dataSourceCfg.getUser());
            readerParam.put("password", decodePassword(dataSourceCfg.getPassword()));

            ArrayNode columnArr = readerParam.putArray("column");
            for (ColumnInfo columnInfo : dataXParameters.getDsColumns()) {
                if (columnInfo.isEnable()) {
                    if (needAddDoubleQuotationMarks){
                        columnArr.add(enclosesStrInQuotationMarks(columnInfo.getColumnName()));
                    }else {
                        columnArr.add(columnInfo.getColumnName());
                    }
                }
            }
            if (StringUtils.isNotEmpty(dataXParameters.getWhere())) {
                readerParam.put("where", dataXParameters.getWhere());
            }

            if (StringUtils.isNotEmpty(dataXParameters.getSplitPk())) {
                readerParam.put("splitPk", dataXParameters.getSplitPk());
            }

            readerParam.putArray("connection").addAll(readerConnArr);
        }

        ObjectNode reader = JSONUtils.createObjectNode();
        reader.put("name", DataxUtils.getReaderPluginName(dataxTaskExecutionContext.getSourcetype()));
        reader.set("parameter", readerParam);
        return reader;
    }

    public ObjectNode buildDataxWriterJson() {
        BaseConnectionParam dataTargetCfg = (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                dataxTaskExecutionContext.getTargetType(),
                dataxTaskExecutionContext.getTargetConnectionParams());
        ObjectNode writerParam = JSONUtils.createObjectNode();
        if (dataxTaskExecutionContext.getTargetType().isHive()) {
            HiveMetadata hiveMetadata = tryGetHiveTableMetadataFromDDL(dataTargetCfg, dataXParameters.getTargetTable());
            ArrayNode columnArr = writerParam.putArray("column");
            for (ColumnInfo columnInfo : dataXParameters.getDtColumns()) {
                if (columnInfo.isEnable()) {
                    columnArr.add(getHiveTargetColumnNodeFromColumnInfo(columnInfo));
                }
            }
            writerParam.put("fileName", dataXParameters.getTargetTable());
            writerParam.put("defaultFS", hiveMetadata.getDefaultFS());
            writerParam.put("path", getHdfsFilePath(hiveMetadata.getPath(), false));
            writerParam.put("fileType", hiveMetadata.getFileType());
            writerParam.put("fieldDelimiter", hiveMetadata.getFieldDelimiter());
            writerParam.put("writeMode", WriteMode.valueOf(dataXParameters.getWriteMode()).getDescp());
        } else if (dataxTaskExecutionContext.getTargetType().isElasticsearch()) {
            ArrayNode columnArr = writerParam.putArray("column");
            for (ColumnInfo columnInfo : dataXParameters.getDtColumns()) {
                if (columnInfo.isEnable()) {
                    columnArr.add(JSONUtils.parseObject(columnInfo.getJson()));
                }
            }
            ElasticSearchDataxParams elasticSearchDataxParams = dataXParameters.getElasticSearchDataxParams();
            writerParam.put("endpoint", dataTargetCfg.getAddress());
            writerParam.put("accessId", dataTargetCfg.getUser());
            writerParam.put("accessKey", dataTargetCfg.getPassword());
            writerParam.put("index", elasticSearchDataxParams.getIndex());
            writerParam.put("type", elasticSearchDataxParams.getType());
            writerParam.put("cleanup", elasticSearchDataxParams.isCleanUp());
            if (elasticSearchDataxParams.getTyrSize() > 0) {
                writerParam.put("trySize", elasticSearchDataxParams.getTyrSize());
            }
            if (elasticSearchDataxParams.getTimeout() > 0) {
                writerParam.put("timeout", elasticSearchDataxParams.getTimeout());
            }
            writerParam.put("discovery", elasticSearchDataxParams.isDiscovery());
            writerParam.put("compression", elasticSearchDataxParams.isCompression());
            writerParam.put("multiThread", elasticSearchDataxParams.isMultiThread());
            writerParam.put("ignoreWriteError", elasticSearchDataxParams.isIgnoreWriteError());
            writerParam.put("ignoreParseError", elasticSearchDataxParams.isIgnoreParseError());
            if (StringUtils.isNotEmpty(elasticSearchDataxParams.getAlias())) {
                writerParam.put("alias", elasticSearchDataxParams.getAlias());
                writerParam.put("aliasMode", AliasMode.valueOf(elasticSearchDataxParams.getAliasMode()).getDescp());
            }
            if (StringUtils.isNotEmpty(elasticSearchDataxParams.getSettings())) {
                writerParam.put("settings", elasticSearchDataxParams.getSettings());
            }
            if (StringUtils.isNotEmpty(elasticSearchDataxParams.getSplitter())) {
                writerParam.put("splitter", elasticSearchDataxParams.getSplitter());
            }
            writerParam.put("dynamic", elasticSearchDataxParams.isDynamic());
            if (dataXParameters.getBatchSize() > 0) {
                writerParam.put("batchSize", dataXParameters.getBatchSize());
            }
        } else {
            DbType targetDbType = dataxTaskExecutionContext.getTargetType();
            List<ObjectNode> writerConnArr = new ArrayList<>();
            ObjectNode writerConn = JSONUtils.createObjectNode();
            ArrayNode tableArr = writerConn.putArray("table");
            String targetTable = dataXParameters.getTargetTable();
            if (needAddDoubleQuotationMarks(targetDbType)){
                tableArr.add(enclosesStrInQuotationMarks(targetTable));
            }else {
                tableArr.add(targetTable);
            }

            writerConn.put("jdbcUrl", DataSourceUtils.getJdbcUrl(DbType.valueOf(dataXParameters.getDtType()), dataTargetCfg));
            writerConnArr.add(writerConn);

            writerParam.put("username", dataTargetCfg.getUser());
            writerParam.put("password", decodePassword(dataTargetCfg.getPassword()));

            // only write to mysql has 3 write modes
            if (dataxTaskExecutionContext.getTargetType().isMysql()) {
                writerParam.put("writeMode", WriteMode.valueOf(dataXParameters.getWriteMode()).getDescp());
            }

            if (dataXParameters.getCustomSQL() == Flag.YES.ordinal()) {
                //  TODO: not support yet
                BaseConnectionParam dataSourceCfg = (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                        dataxTaskExecutionContext.getSourcetype(),
                        dataxTaskExecutionContext.getSourceConnectionParams());

                String[] columns = parsingSqlColumnNames(dataxTaskExecutionContext.getSourcetype(),
                        dataxTaskExecutionContext.getTargetType(),
                        dataSourceCfg, dataXParameters.getSql());

                ArrayNode columnArr = writerParam.putArray("column");
                for (String column : columns) {
                    columnArr.add(column);
                }
            } else {
                ArrayNode columnArr = writerParam.putArray("column");
                for (ColumnInfo columnInfo : dataXParameters.getDtColumns()) {
                    if (needAddDoubleQuotationMarks(targetDbType)){
                        columnArr.add(enclosesStrInQuotationMarks(columnInfo.getColumnName()));
                    }else {
                        columnArr.add(columnInfo.getColumnName());
                    }
                }
            }
            writerParam.putArray("connection").addAll(writerConnArr);

            if (CollectionUtils.isNotEmpty(dataXParameters.getPreStatements())) {
                ArrayNode preSqlArr = writerParam.putArray("preSql");
                for (String preSql : dataXParameters.getPreStatements()) {
                    preSqlArr.add(preSql);
                }

            }

            if (CollectionUtils.isNotEmpty(dataXParameters.getPostStatements())) {
                ArrayNode postSqlArr = writerParam.putArray("postSql");
                for (String postSql : dataXParameters.getPostStatements()) {
                    postSqlArr.add(postSql);
                }
            }

            if (dataXParameters.getBatchSize() > 0) {
                writerParam.put("batchSize", dataXParameters.getBatchSize());
            }
        }


        ObjectNode writer = JSONUtils.createObjectNode();
        writer.put("name", DataxUtils.getWriterPluginName(dataxTaskExecutionContext.getTargetType()));
        writer.set("parameter", writerParam);
        return writer;
    }

    /**
     * build datax setting config
     *
     * @return datax setting config JSONObject
     */
    private ObjectNode buildDataxJobSettingJson() {

        ObjectNode speed = JSONUtils.createObjectNode();

        // job speed for per channel
//        if (dataXParameters.getJobSpeedByte() > 0) {
//            speed.put("byte", dataXParameters.getJobSpeedByte());
//        }
//
//        if (dataXParameters.getJobSpeedRecord() > 0) {
//            speed.put("record", dataXParameters.getJobSpeedRecord());
//        }

        speed.put("channel", dataXParameters.getChannel() == 0 ? DATAX_CHANNEL_COUNT : dataXParameters.getChannel());

        ObjectNode errorLimit = JSONUtils.createObjectNode();
        errorLimit.put("record", 0);
        errorLimit.put("percentage", 0);

        ObjectNode setting = JSONUtils.createObjectNode();
        setting.set("speed", speed);
        setting.set("errorLimit", errorLimit);

        return setting;
    }

    private ObjectNode buildDataxCoreJson() {

        ObjectNode speed = JSONUtils.createObjectNode();

        if (dataXParameters.getJobSpeedByte() > 0) {
            speed.put("byte", dataXParameters.getJobSpeedByte());
        }

        if (dataXParameters.getJobSpeedRecord() > 0) {
            speed.put("record", dataXParameters.getJobSpeedRecord());
        }

        ObjectNode channel = JSONUtils.createObjectNode();
        channel.set("speed", speed);

        ObjectNode transport = JSONUtils.createObjectNode();
        transport.set("channel", channel);

        ObjectNode core = JSONUtils.createObjectNode();
        core.set("transport", transport);

        return core;
    }

    /**
     * create command
     *
     * @return shell command file name
     * @throws Exception if error throws Exception
     */
    private String buildShellCommandFile(String jobConfigFilePath, Map<String, Property> paramsMap)
            throws Exception {
        // generate scripts
        String fileName = String.format("%s/%s_node.%s",
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId(),
                OSUtils.isWindows() ? "bat" : "sh");

        Path path = new File(fileName).toPath();

        if (Files.exists(path)) {
            return fileName;
        }

        // datax python command
        StringBuilder sbr = new StringBuilder();
        sbr.append(getPythonCommand());
        sbr.append(" ");
        sbr.append(DATAX_PATH);
        sbr.append(" ");
        sbr.append(loadJvmEnv(dataXParameters));
        sbr.append(jobConfigFilePath);

        // replace placeholder
        String dataxCommand = ParameterUtils.convertParameterPlaceholders(sbr.toString(), ParamUtils.convert(paramsMap));

        logger.debug("raw script : {}", dataxCommand);

        // create shell command file
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString(RWXR_XR_X);
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        if (OSUtils.isWindows()) {
            Files.createFile(path);
        } else {
            Files.createFile(path, attr);
        }

        Files.write(path, dataxCommand.getBytes(), StandardOpenOption.APPEND);

        return fileName;
    }

    public String getPythonCommand() {
        return System.getenv("PYTHON_HOME");
    }

    public String loadJvmEnv(DataxParameters dataXParameters) {
        int xms = Math.max(dataXParameters.getXms(), 1);
        int xmx = Math.max(dataXParameters.getXmx(), 1);
        return String.format(JVM_PARAM, xms, xmx);
    }

    /**
     * parsing synchronized column names in SQL statements
     *
     * @param sourceType the database type of the data source
     * @param targetType the database type of the data target
     * @param dataSourceCfg the database connection parameters of the data source
     * @param sql sql for data synchronization
     * @return Keyword converted column names
     */
    private String[] parsingSqlColumnNames(DbType sourceType, DbType targetType, BaseConnectionParam dataSourceCfg, String sql) {
        String[] columnNames = tryGrammaticalAnalysisSqlColumnNames(sourceType, sql);

        if (columnNames == null || columnNames.length == 0) {
            logger.info("try to execute sql analysis query column name");
            columnNames = tryExecuteSqlResolveColumnNames(sourceType, dataSourceCfg, sql);
        }

        notNull(columnNames, String.format("parsing sql columns failed : %s", sql));

        return DataxUtils.convertKeywordsColumns(targetType, columnNames);
    }

    /**
     * try grammatical parsing column
     *
     * @param dbType database type
     * @param sql sql for data synchronization
     * @return column name array
     * @throws RuntimeException if error throws RuntimeException
     */
    private String[] tryGrammaticalAnalysisSqlColumnNames(DbType dbType, String sql) {
        String[] columnNames;

        try {
            SQLStatementParser parser = DataxUtils.getSqlStatementParser(dbType, sql);
            if (parser == null) {
                logger.warn("database driver [{}] is not support grammatical analysis sql", dbType);
                return new String[0];
            }

            SQLStatement sqlStatement = parser.parseStatement();
            SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
            SQLSelect sqlSelect = sqlSelectStatement.getSelect();

            List<SQLSelectItem> selectItemList = null;
            if (sqlSelect.getQuery() instanceof SQLSelectQueryBlock) {
                SQLSelectQueryBlock block = (SQLSelectQueryBlock) sqlSelect.getQuery();
                selectItemList = block.getSelectList();
            } else if (sqlSelect.getQuery() instanceof SQLUnionQuery) {
                SQLUnionQuery unionQuery = (SQLUnionQuery) sqlSelect.getQuery();
                SQLSelectQueryBlock block = (SQLSelectQueryBlock) unionQuery.getRight();
                selectItemList = block.getSelectList();
            }

            notNull(selectItemList,
                    String.format("select query type [%s] is not support", sqlSelect.getQuery().toString()));

            columnNames = new String[selectItemList.size()];
            for (int i = 0; i < selectItemList.size(); i++) {
                SQLSelectItem item = selectItemList.get(i);

                String columnName = null;

                if (item.getAlias() != null) {
                    columnName = item.getAlias();
                } else if (item.getExpr() != null) {
                    if (item.getExpr() instanceof SQLPropertyExpr) {
                        SQLPropertyExpr expr = (SQLPropertyExpr) item.getExpr();
                        columnName = expr.getName();
                    } else if (item.getExpr() instanceof SQLIdentifierExpr) {
                        SQLIdentifierExpr expr = (SQLIdentifierExpr) item.getExpr();
                        columnName = expr.getName();
                    }
                } else {
                    throw new RuntimeException(
                            String.format("grammatical analysis sql column [ %s ] failed", item.toString()));
                }

                if (columnName == null) {
                    throw new RuntimeException(
                            String.format("grammatical analysis sql column [ %s ] failed", item.toString()));
                }

                columnNames[i] = columnName;
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return new String[0];
        }

        return columnNames;
    }

    /**
     * try to execute sql to resolve column names
     *
     * @param baseDataSource the database connection parameters
     * @param sql sql for data synchronization
     * @return column name array
     */
    public String[] tryExecuteSqlResolveColumnNames(DbType sourceType, BaseConnectionParam baseDataSource, String sql) {
        String[] columnNames;
        sql = String.format("SELECT t.* FROM ( %s ) t WHERE 0 = 1", sql);
        sql = sql.replace(";", "");

        try (
                Connection connection = DataSourceClientProvider.getInstance().getConnection(sourceType, baseDataSource);
                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet resultSet = stmt.executeQuery()) {

            ResultSetMetaData md = resultSet.getMetaData();
            int num = md.getColumnCount();
            columnNames = new String[num];
            for (int i = 1; i <= num; i++) {
                columnNames[i - 1] = md.getColumnName(i);
            }
        } catch (SQLException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            return null;
        }

        return columnNames;
    }

    @Override
    public AbstractParameters getParameters() {
        return dataXParameters;
    }

    private void notNull(Object obj, String message) {
        if (obj == null) {
            throw new RuntimeException(message);
        }
    }

    public HiveMetadata tryGetHiveTableMetadataFromDDL(BaseConnectionParam baseDataSource, String table) {
        String sql = String.format("SHOW CREATE TABLE %s", table);

        try (
                Connection connection = DataSourceClientProvider.getInstance().getConnection(DbType.HIVE, baseDataSource);
                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet resultSet = stmt.executeQuery()) {
            StringBuilder ddlBuilder = new StringBuilder();
            while (resultSet.next()){
                ddlBuilder.append(resultSet.getString(1)).append('\n');
            }
            String ddl = ddlBuilder.toString();
            Pattern fieldDelimiterPattern = Pattern.compile("'field.delim'='(.*?)'");
            Matcher fieldDelimiterMatcher = fieldDelimiterPattern.matcher(ddl);
            Pattern inputFormatPattern = Pattern.compile("INPUTFORMAT\\s*\\n\\s*'(.*?)'");
            Matcher inputFormatMatcher = inputFormatPattern.matcher(ddl);
            Pattern locationPattern = Pattern.compile("LOCATION\\s*\\n\\s*'(.*?)'");
            Matcher locationMatcher = locationPattern.matcher(ddl);
            HiveMetadata hiveMetadata = new HiveMetadata();
            if (inputFormatMatcher.find()){
                String input = inputFormatMatcher.group(1);
                if (input.contains("TextInputFormat")){
                    hiveMetadata.setFileType("text");
                }else if (input.contains("OrcInputFormat")){
                    hiveMetadata.setFileType("orc");
                }else {
                    throw new RuntimeException("parse hive metadata failed: only support orcfile and textfile");
                }
            }
            if (fieldDelimiterMatcher.find()){
                String fd = fieldDelimiterMatcher.group(1);
                fd = fd.replace("\\t","\t");
                hiveMetadata.setFieldDelimiter(fd);
            }else {
                if (hiveMetadata.getFileType().equals("orc")){
                    logger.warn("table is orcfile, set fieldDelimiter default '\\t'");
                    hiveMetadata.setFieldDelimiter("\t");
                }else {
                    throw new RuntimeException("parse hive metadata failed: cannot find fieldDelimiter");
                }
            }
            if (locationMatcher.find()) {
                String location = locationMatcher.group(1);
                hiveMetadata.setLocation(location);
            }
            if(!hiveMetadata.checkParameters()){
                throw new RuntimeException("parse hive metadata from ddl failed");
            }
            return hiveMetadata;
        } catch (SQLException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private ObjectNode getHiveTargetColumnNodeFromColumnInfo(ColumnInfo columnInfo){
        ObjectNode columnNode = JSONUtils.createObjectNode();
        columnNode.put("name", columnInfo.getColumnName());
        columnNode.put("type",columnInfo.getDataType());
        return columnNode;
    }

    private ObjectNode getHiveSourceColumnNodeFromColumnInfo(ColumnInfo columnInfo){
        ObjectNode columnNode = JSONUtils.createObjectNode();
        if(columnInfo.getIndex() >= 0){
            columnNode.put("index", columnInfo.getIndex());
            // TODO: hardcode dataType as string
            columnNode.put("type","string");
        }else {
            columnNode.put("value", columnInfo.getColumnName());
            columnNode.put("type","string");
        }
        return columnNode;
    }

    private String getHdfsFilePath(String basePath, boolean isReader){
        if(isReader){
            if (CollectionUtils.isNotEmpty(dataXParameters.getDsPartitions())){
                return String.format("%s/%s/*",basePath, StringUtils.join(dataXParameters.getDsPartitions(),"/"));
            }
            return basePath + "/*";
        }else {
            if (CollectionUtils.isNotEmpty(dataXParameters.getDtPartitions())){
                return String.format("%s/%s",basePath, StringUtils.join(dataXParameters.getDtPartitions(),"/"));
            }
            return basePath;
        }
    }

    /**
     * when the dbtype is oracle or dameng or postgresql， need to enclose table name and column name in quotation marks. (in case the database is case sensitive)
     * @param dbType
     * @return
     */
    private boolean needAddDoubleQuotationMarks(DbType dbType){
        return dbType.isPgSQL() || dbType.isDaMeng() || dbType.isOracle();
    }

    private String enclosesStrInQuotationMarks(String str){
        return "\"" + str + "\"";
    }
}
