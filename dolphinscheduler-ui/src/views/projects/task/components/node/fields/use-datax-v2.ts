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
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { IJsonItem } from '../types'
import { isNumber } from 'lodash'
import {
  TableColumnsWithType,
  TypeReq
} from '@/service/modules/data-source/types'
import {
  getDatasourceTablesById,
  queryDataSourceList,
  queryTableColumnsWithType
} from '@/service/modules/data-source'
import { fieldMappingListItem } from '@/components/form/types'

export function useDataX(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const datasourceTypes = [
    {
      id: 0,
      code: 'MYSQL',
      disabled: false
    },
    {
      id: 1,
      code: 'POSTGRESQL',
      disabled: false
    },
    {
      id: 2,
      code: 'HIVE',
      disabled: false
    },
    {
      id: 3,
      code: 'SPARK',
      disabled: true
    },
    {
      id: 4,
      code: 'CLICKHOUSE',
      disabled: false
    },
    {
      id: 5,
      code: 'ORACLE',
      disabled: false
    },
    {
      id: 6,
      code: 'SQLSERVER',
      disabled: false
    },
    {
      id: 7,
      code: 'DB2',
      disabled: true
    },
    {
      id: 8,
      code: 'PRESTO',
      disabled: true
    },
    {
      id: 9,
      code: 'ELASTICSEARCH',
      disabled: true
    },
    {
      id: 10,
      code: 'DM',
      disabled: false
    }
  ]
  const dtTypes = [
    {
      id: 0,
      code: 'MYSQL',
      disabled: false
    },
    {
      id: 1,
      code: 'POSTGRESQL',
      disabled: false
    },
    {
      id: 2,
      code: 'HIVE',
      disabled: false
    },
    {
      id: 3,
      code: 'SPARK',
      disabled: true
    },
    {
      id: 4,
      code: 'CLICKHOUSE',
      disabled: false
    },
    {
      id: 5,
      code: 'ORACLE',
      disabled: false
    },
    {
      id: 6,
      code: 'SQLSERVER',
      disabled: false
    },
    {
      id: 7,
      code: 'DB2',
      disabled: true
    },
    {
      id: 8,
      code: 'PRESTO',
      disabled: true
    },
    {
      id: 9,
      code: 'ELASTICSEARCH',
      disabled: false
    },
    {
      id: 10,
      code: 'DM',
      disabled: false
    }
  ]
  const datasourceTypeOptions = ref([] as any)
  const dtTypeOptions = ref([] as any)
  const datasourceOptions = ref([] as any)
  const sourceTableOptions = ref([] as any)
  const targetTableOptions = ref([] as any)
  const destinationDatasourceOptions = ref([] as any)
  const aliasModeOptions = [
    { label: 'append', value: 0 },
    { label: 'exclusive', value: 1 }
  ]
  const jobSpeedByteOptions: any[] = [
    {
      label: `0(${t('project.node.unlimited')})`,
      value: 0
    },
    {
      label: '1KB',
      value: 1024
    },
    {
      label: '10KB',
      value: 10240
    },
    {
      label: '50KB',
      value: 51200
    },
    {
      label: '100KB',
      value: 102400
    },
    {
      label: '512KB',
      value: 524288
    }
  ]
  const jobSpeedRecordOptions: any[] = [
    {
      label: `0(${t('project.node.unlimited')})`,
      value: 0
    },
    {
      label: '500',
      value: 500
    },
    {
      label: '1000',
      value: 1000
    },
    {
      label: '1500',
      value: 1500
    },
    {
      label: '2000',
      value: 2000
    },
    {
      label: '2500',
      value: 2500
    },
    {
      label: '3000',
      value: 3000
    }
  ]
  // const memoryLimitOptions = [
  //   {
  //     label: '1G',
  //     value: 1
  //   },
  //   {
  //     label: '2G',
  //     value: 2
  //   },
  //   {
  //     label: '3G',
  //     value: 3
  //   },
  //   {
  //     label: '4G',
  //     value: 4
  //   }
  // ]
  const loading = ref(false)
  const writeModeOption = [
    {
      label: 'insert into（当主键/约束冲突报脏数据）',
      value: '0'
    },
    {
      label: 'replace into（当主键/约束冲突时替换）',
      value: '1'
    },
    {
      label: 'on duplicate key update（当主键/约束冲突时更新）',
      value: '2'
    }
  ]
  const ruleModeOption = [
    {
      label: 'append（写入前不做任何处理）',
      value: '3'
    },
    {
      label: 'nonConflict（如果已经存在数据则报错）',
      value: '4'
    },
    {
      label: 'truncate（写入前清理已有数据）',
      value: '5'
    }
  ]

  const getDatasourceTypes = async () => {
    if (loading.value) return
    loading.value = true
    datasourceTypeOptions.value = datasourceTypes
      .filter((item) => !item.disabled)
      .map((item) => ({ label: item.code, value: item.code }))
    loading.value = false
  }
  const getDtTypes = async () => {
    if (loading.value) return
    loading.value = true
    dtTypeOptions.value = dtTypes
      .filter((item) => !item.disabled)
      .map((item) => ({ label: item.code, value: item.code }))
    loading.value = false
  }

  const getDatasourceInstances = async () => {
    const params = { type: model.dsType } as TypeReq
    const res = await queryDataSourceList(params)
    datasourceOptions.value = []
    res.map((item: any) => {
      datasourceOptions.value.push({ label: item.name, value: String(item.id) })
    })
  }

  const getSourceTableOptions = async () => {
    if (!model.dataSource) return null
    sourceTableOptions.value = []
    const res = await getDatasourceTablesById(model.dataSource)
    res.map((item: any) => {
      sourceTableOptions.value.push(item)
    })
  }

  const getTargetTableOptions = async () => {
    if (!model.dataTarget) return null
    targetTableOptions.value = []
    const res = await getDatasourceTablesById(model.dataTarget)
    res.map((item: any) => {
      targetTableOptions.value.push(item)
    })
  }

  const getDestinationDatasourceInstances = async () => {
    const params = { type: model.dtType } as TypeReq
    const res = await queryDataSourceList(params)
    destinationDatasourceOptions.value = []
    res.map((item: any) => {
      destinationDatasourceOptions.value.push({
        label: item.name,
        value: String(item.id)
      })
    })
  }

  const getDspartitionsInstances = async () => {
    if (!model.sourceTable || !model.dataSource) {
      model.dsPartitions = []
      return null
    }
    const params = {
      datasourceId: model.dataSource,
      tableName: model.sourceTable
    } as TableColumnsWithType
    const res = await queryTableColumnsWithType(params)
    const { partitionTable, partitionColumns, columns } = res
    model.dsColumns = columns || []
    if (partitionTable) {
      model.dsPartitions = partitionColumns.map((s: any) => s.columnName + '=')
    } else {
      model.dsPartitions = []
    }
  }

  const getDtpartitionsInstances = async () => {
    if (!model.targetTable || !model.dataTarget) {
      model.dtPartitions = []
      return null
    }
    const params = {
      datasourceId: model.dataTarget,
      tableName: model.targetTable
    } as TableColumnsWithType
    const res = await queryTableColumnsWithType(params)
    const { partitionTable, partitionColumns, columns } = res
    model.dtColumns = columns || []
    if (partitionTable) {
      model.dtPartitions = partitionColumns.map((s: any) => s.columnName + '=')
    } else {
      model.dtPartitions = []
    }
  }

  // const sqlEditorSpan = ref(24)
  const jsonEditorSpan = ref(0)
  const datasourceSpan = ref(12)
  const sourceTableSpan = ref(24)
  const targetTableSpan = ref(24)
  const whereSpan = ref(24)
  const dsPartitionsSpan = ref(24)
  const dtPartitionsSpan = ref(24)
  const destinationDatasourceSpan = ref(12)
  const writeModeSpan = ref(24)
  const ruleModeSpan = ref(0)
  const otherStatementSpan = ref(22)
  const jobSpeedSpan = ref(12)
  const customParameterSpan = ref(0)
  const elasticSearchDataxParamsSpan = ref(24)
  const elasticSearchDataxParamsHalfSpan = ref(12)

  const initConstants = () => {
    if (model.customConfig) {
      // sqlEditorSpan.value = 0
      whereSpan.value = 0
      jsonEditorSpan.value = 24
      datasourceSpan.value = 0
      sourceTableSpan.value = 0
      destinationDatasourceSpan.value = 0
      writeModeSpan.value = 0
      ruleModeSpan.value = 0
      targetTableSpan.value = 0
      otherStatementSpan.value = 0
      jobSpeedSpan.value = 0
      customParameterSpan.value = 0
      elasticSearchDataxParamsSpan.value = 0
      elasticSearchDataxParamsHalfSpan.value = 0
      dsPartitionsSpan.value = 0
    } else {
      // sqlEditorSpan.value = 24
      jsonEditorSpan.value = 0
      datasourceSpan.value = 12
      sourceTableSpan.value = 24
      destinationDatasourceSpan.value = 12
      targetTableSpan.value = 24
      otherStatementSpan.value = 22
      jobSpeedSpan.value = 12
      customParameterSpan.value = 24
      dtPartitionsSpan.value = 0
      ruleModeSpan.value = 0
      whereSpan.value = 24
      dsPartitionsSpan.value = 0
      elasticSearchDataxParamsSpan.value = 0
      elasticSearchDataxParamsHalfSpan.value = 0
      writeModeSpan.value = 0
      TypeToWatch()
    }
  }

  onMounted(async () => {
    await getDatasourceTypes()
    await getDtTypes()
    await getDatasourceInstances()
    await getDestinationDatasourceInstances()
    await getSourceTableOptions()
    if (model.dtType !== 'ELASTICSEARCH') await getTargetTableOptions()
    initConstants()
  })

  const TypeToWatch = () => {
    if (model.dsType === 'HIVE') {
      whereSpan.value = 0
      dsPartitionsSpan.value = 24
    }
    if (model.dtType === 'MYSQL') {
      writeModeSpan.value = 24
    }
    if (model.dtType === 'HIVE') {
      otherStatementSpan.value = 0
      dtPartitionsSpan.value = 24
      ruleModeSpan.value = 24
    }
    if (model.dtType === 'ELASTICSEARCH') {
      otherStatementSpan.value = 0
      elasticSearchDataxParamsSpan.value = 24
      elasticSearchDataxParamsHalfSpan.value = 12
      targetTableSpan.value = 0
    }
  }

  const onSourceTypeChange = (type: string) => {
    model.dsType = type
    model.sourceTable = ''
    model.dataSource = ''
    datasourceOptions.value = []
    sourceTableOptions.value = []
    model.dsColumns = []
    initConstants()
    getDatasourceInstances()
  }

  const onDestinationTypeChange = (type: string) => {
    model.dtType = type
    model.targetTable = ''
    model.dataTarget = ''
    model.writeMode = ''
    targetTableOptions.value = []
    destinationDatasourceOptions.value = []
    model.dtColumns = []
    initConstants()
    getDestinationDatasourceInstances()
  }

  const onDatasourceChange = (type: string) => {
    model.dataSource = type
    model.sourceTable = null
    getSourceTableOptions()
  }

  const onDataTargetChange = (type: string) => {
    model.dataTarget = type
    model.targetTable = null
    if (model.dtType !== 'ELASTICSEARCH') getTargetTableOptions()
    else model.dtColumns = []
  }

  const onSourceTableChange = async (type: string) => {
    model.sourceTable = type
    getDspartitionsInstances()
  }

  const onTargetTableChange = async (type: string) => {
    model.targetTable = type
    getDtpartitionsInstances()
  }

  watch(
    () => model.customConfig,
    () => {
      initConstants()
    }
  )

  return [
    {
      type: 'divider',
      field: 'divider' + String(Date.now() + Math.random()),
      span: sourceTableSpan,
      props: {
        title: t('project.node.data_select_in_divider')
      }
    },
    {
      type: 'switch',
      field: 'customConfig',
      name: t('project.node.datax_custom_template')
    },
    {
      type: 'select',
      field: 'dsType',
      span: datasourceSpan,
      name: t('project.node.datasource_type'),
      props: {
        loading: loading,
        'on-update:value': onSourceTypeChange
      },
      options: datasourceTypeOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'select',
      field: 'dataSource',
      span: datasourceSpan,
      name: t('project.node.datasource_instances'),
      props: {
        loading: loading,
        'on-update:value': onDatasourceChange
      },
      options: datasourceOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'select',
      field: 'sourceTable',
      span: sourceTableSpan,
      name: t('project.node.table'),
      options: sourceTableOptions,
      props: {
        'on-update:value': onSourceTableChange
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'input',
      field: 'where',
      name: t('project.node.data_filter'),
      span: whereSpan,
      props: {
        placeholder: t('project.node.data_filter_tips'),
        type: 'textarea',
        autosize: { minRows: 3 }
      }
    },
    {
      type: 'input',
      field: 'splitPk',
      name: t('project.node.split_field'),
      span: whereSpan,
      props: {
        placeholder: t('project.node.split_field_tips'),
        type: 'textarea',
        autosize: { minRows: 2 }
      }
    },
    {
      type: 'dspartition-input',
      field: 'dsPartitions',
      name: t('project.node.dsPartitions_field'),
      span: dsPartitionsSpan,
      props: {
        placeholder: t('project.node.dsPartitions_field_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value.length) {
            return true
          }
          for (const valueElement of value) {
            const [, v] = valueElement.split('=')
            if (!v) {
              return new Error(t('project.node.dsPartitions_field_tips'))
            }
          }
          return true
        }
      }
    },
    // {
    //   type: 'editor',
    //   field: 'sql',
    //   name: t('project.node.sql_statement'),
    //   span: sqlEditorSpan,
    //   validate: {
    //     trigger: ['input', 'trigger'],
    //     required: true,
    //     message: t('project.node.sql_empty_tips')
    //   }
    // },
    {
      type: 'editor',
      field: 'json',
      name: t('project.node.datax_json_template'),
      span: jsonEditorSpan,
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.sql_empty_tips')
      }
    },
    {
      type: 'divider',
      field: 'divider2',
      span: sourceTableSpan,
      props: {
        title: t('project.node.data_select_out_divider')
      }
    },
    {
      type: 'select',
      field: 'dtType',
      name: t('project.node.datax_target_datasource_type'),
      span: destinationDatasourceSpan,
      props: {
        loading: loading,
        'on-update:value': onDestinationTypeChange
      },
      options: dtTypeOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'select',
      field: 'dataTarget',
      name: t('project.node.datax_target_database'),
      span: destinationDatasourceSpan,
      props: {
        loading: loading,
        'on-update:value': onDataTargetChange
      },
      options: destinationDatasourceOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'select',
      field: 'targetTable',
      span: targetTableSpan,
      name: t('project.node.table'),
      props: {
        'on-update:value': onTargetTableChange
      },
      options: targetTableOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'multi-input',
      field: 'preStatements',
      name: t('project.node.datax_target_database_pre_sql'),
      span: otherStatementSpan,
      props: {
        placeholder: t('project.node.datax_non_query_sql_tips'),
        type: 'textarea',
        autosize: { minRows: 1 }
      }
    },
    {
      type: 'multi-input',
      field: 'postStatements',
      name: t('project.node.datax_target_database_post_sql'),
      span: otherStatementSpan,
      props: {
        placeholder: t('project.node.datax_non_query_sql_tips'),
        type: 'textarea',
        autosize: { minRows: 1 }
      }
    },
    {
      type: 'dspartition-input',
      field: 'dtPartitions',
      name: t('project.node.dsPartitions_field'),
      span: dtPartitionsSpan,
      props: {
        placeholder: t('project.node.dsPartitions_field_tips'),
        showTips: true
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value.length) {
            return true
          }
          for (const valueElement of value) {
            const [, v] = valueElement.split('=')
            if (!v) {
              return new Error(t('project.node.dsPartitions_field_tips'))
            }
          }
          return true
        }
      }
    },
    {
      type: 'select',
      field: 'writeMode',
      name: t('project.node.write_mode_field'),
      span: writeModeSpan,
      options: writeModeOption,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'select',
      field: 'writeMode',
      name: t('project.node.rule_mode_field'),
      span: ruleModeSpan,
      options: ruleModeOption,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'divider',
      field: 'divider' + String(Date.now() + Math.random()),
      span: elasticSearchDataxParamsSpan,
      props: {
        title: t('project.node.elastic_search_datax_params_setting')
      }
    },
    {
      type: 'input',
      field: 'elasticSearchDataxParams.index',
      name: t('project.node.elastic_search_datax_params_index_field'),
      span: elasticSearchDataxParamsSpan,
      props: {
        placeholder: t(
          'project.node.elastic_search_datax_params_field_index_tips'
        )
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'input',
      field: 'elasticSearchDataxParams.type',
      name: t('project.node.elastic_search_datax_params_type_field'),
      span: elasticSearchDataxParamsSpan,
      props: {
        placeholder: t(
          'project.node.elastic_search_datax_params_field_type_tips'
        )
      }
    },
    {
      type: 'switch',
      field: 'elasticSearchDataxParams.cleanUp',
      name: t('project.node.elastic_search_datax_params_cleanUp_field'),
      span: elasticSearchDataxParamsSpan
    },
    {
      type: 'input',
      field: 'elasticSearchDataxParams.splitter',
      name: t('project.node.elastic_search_datax_params_splitter_field'),
      span: elasticSearchDataxParamsSpan,
      props: {
        placeholder: t(
          'project.node.elastic_search_datax_params_field_splitter_tips'
        )
      }
    },
    {
      type: 'input',
      field: 'elasticSearchDataxParams.alias',
      name: t('project.node.elastic_search_datax_params_alias_field'),
      span: elasticSearchDataxParamsSpan,
      props: {
        placeholder: t(
          'project.node.elastic_search_datax_params_field_alias_tips'
        )
      }
    },
    {
      type: 'select',
      field: 'elasticSearchDataxParams.aliasMode',
      name: t('project.node.elastic_search_datax_params_aliasMode_field'),
      span: elasticSearchDataxParamsSpan,
      options: aliasModeOptions,
      props: {
        placeholder: t(
          'project.node.elastic_search_datax_params_field_aliasMode_tips'
        )
      }
    },
    {
      type: 'editor',
      field: 'elasticSearchDataxParams.settings',
      name: t('project.node.elastic_search_datax_params_setting_field'),
      span: elasticSearchDataxParamsSpan
    },
    {
      type: 'divider',
      field: 'divider' + String(Date.now() + Math.random()),
      span: elasticSearchDataxParamsSpan,
      props: {
        title: t('project.node.elastic_search_datax_params_setting_advanced')
      }
    },
    {
      type: 'switch',
      field: 'elasticSearchDataxParams.ignoreWriteError',
      name: t(
        'project.node.elastic_search_datax_params_ignoreWriteError_field'
      ),
      span: elasticSearchDataxParamsHalfSpan
    },
    {
      type: 'switch',
      field: 'elasticSearchDataxParams.ignoreParseError',
      name: t(
        'project.node.elastic_search_datax_params_ignoreParseError_field'
      ),
      span: elasticSearchDataxParamsHalfSpan
    },
    {
      type: 'input-number',
      field: 'elasticSearchDataxParams.tyrSize',
      name: t('project.node.elastic_search_datax_params_tyrSize_field'),
      span: elasticSearchDataxParamsHalfSpan,
      props: {
        placeholder: t(
          'project.node.elastic_search_datax_params_field_tyrSize_tips'
        ),
        min: 0,
        style: 'width:100%'
      }
    },
    {
      type: 'input-number',
      field: 'elasticSearchDataxParams.timeout',
      name: t('project.node.elastic_search_datax_params_timeout_field'),
      span: elasticSearchDataxParamsHalfSpan,
      props: {
        placeholder: t(
          'project.node.elastic_search_datax_params_field_timeout_tips'
        ),
        min: 0,
        style: 'width:100%'
      }
    },
    {
      type: 'switch',
      field: 'elasticSearchDataxParams.discovery',
      name: t('project.node.elastic_search_datax_params_discovery_field'),
      span: elasticSearchDataxParamsHalfSpan
    },
    {
      type: 'switch',
      field: 'elasticSearchDataxParams.compression',
      name: t('project.node.elastic_search_datax_params_compression_field'),
      span: elasticSearchDataxParamsHalfSpan
    },
    {
      type: 'switch',
      field: 'elasticSearchDataxParams.multiThread',
      name: t('project.node.elastic_search_datax_params_multiThread_field'),
      span: elasticSearchDataxParamsHalfSpan
    },
    {
      type: 'switch',
      field: 'elasticSearchDataxParams.dynamic',
      name: t('project.node.elastic_search_datax_params_dynamic_field'),
      span: elasticSearchDataxParamsHalfSpan
    },
    {
      type: 'divider',
      field: 'divider' + String(Date.now() + Math.random()),
      span: sourceTableSpan,
      props: {
        title: t('project.node.field_mapping_label')
      }
    },
    {
      type: 'field-mapping',
      field: 'dsColumns:dtColumns',
      name: t('project.node.field_mapping_label'),
      span: sourceTableSpan,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator() {
          if (
            model.dtType !== 'ELASTICSEARCH' &&
            (model.dsColumns.some((s: fieldMappingListItem) => !s.columnName) ||
              model.dtColumns.some((s: fieldMappingListItem) => !s.columnName))
          ) {
            return new Error(t('project.node.field_mapping_name_tips'))
          }
          if (
            model.dtType !== 'ELASTICSEARCH' &&
            (model.dsColumns.some((s: fieldMappingListItem) => !s.dataType) ||
              model.dtColumns.some((s: fieldMappingListItem) => !s.dataType))
          ) {
            return new Error(t('project.node.field_mapping_type_tips'))
          }
          if (
            model.dtType === 'ELASTICSEARCH' &&
            (model.dsColumns.some((s: fieldMappingListItem) => !s.columnName) ||
              model.dtColumns.some((s: fieldMappingListItem) => !s.json))
          ) {
            return new Error(t('project.node.field_mapping_name_tips'))
          }
          return true
        }
      }
    },
    {
      type: 'divider',
      field: 'divider' + String(Date.now() + Math.random()),
      span: sourceTableSpan,
      props: {
        title: t('project.node.channel_control')
      }
    },
    {
      type: 'custom-parameters',
      field: 'localParams',
      name: t('project.node.custom_parameters'),
      span: customParameterSpan,
      children: [
        {
          type: 'input',
          field: 'prop',
          span: 10,
          props: {
            placeholder: t('project.node.prop_tips'),
            maxLength: 256
          },
          validate: {
            trigger: ['input', 'blur'],
            required: true,
            validator(validate: any, value: string) {
              if (!value) {
                return new Error(t('project.node.prop_tips'))
              }

              const sameItems = model.localParams.filter(
                (item: { prop: string }) => item.prop === value
              )

              if (sameItems.length > 1) {
                return new Error(t('project.node.prop_repeat'))
              }
            }
          }
        },
        {
          type: 'input',
          field: 'value',
          span: 10,
          props: {
            placeholder: t('project.node.value_tips'),
            maxLength: 256
          }
        }
      ]
    },
    {
      type: 'input-number',
      field: 'batchSize',
      name: t('project.node.field_batchSize'),
      span: datasourceSpan,
      props: {
        placeholder: t('project.node.field_batchSize'),
        min: 0,
        style: 'width:100%'
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        validator(v, value) {
          if (!isNumber(value)) {
            return new Error(t('project.node.field_batchSize_tips'))
          }
          return true
        }
      },
      value: 1024
    },
    {
      type: 'input-number',
      field: 'channel',
      name: t('project.node.field_channel'),
      span: datasourceSpan,
      props: {
        placeholder: t('project.node.field_channel'),
        min: 0,
        style: 'width:100%'
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        validator(v, value) {
          if (!isNumber(value)) {
            return new Error(t('project.node.field_channel_tips'))
          }
          return true
        }
      },
      value: 1
    },
    {
      type: 'select',
      field: 'jobSpeedByte',
      name: t('project.node.datax_job_speed_byte'),
      span: jobSpeedSpan,
      options: jobSpeedByteOptions,
      value: 0
    },
    {
      type: 'select',
      field: 'jobSpeedRecord',
      name: t('project.node.datax_job_speed_record'),
      span: jobSpeedSpan,
      options: jobSpeedRecordOptions,
      value: 0
    },
    {
      type: 'input-number',
      field: 'xms',
      name: t('project.node.datax_job_runtime_memory_xms'),
      span: 12,
      props: {
        min: 1,
        style: 'width:100%'
      },
      slots: {
        suffix: () => t('project.node.gb')
      },
      value: 1
    },
    {
      type: 'input-number',
      field: 'xmx',
      name: t('project.node.datax_job_runtime_memory_xmx'),
      span: 12,
      props: {
        min: 1,
        style: 'width:100%'
      },
      slots: {
        suffix: () => t('project.node.gb')
      },
      value: 5
    }
  ]
}
