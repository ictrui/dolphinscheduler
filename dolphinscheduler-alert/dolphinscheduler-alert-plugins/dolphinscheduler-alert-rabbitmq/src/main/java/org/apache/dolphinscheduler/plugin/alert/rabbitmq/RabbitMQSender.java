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

package org.apache.dolphinscheduler.plugin.alert.rabbitmq;


import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class RabbitMQSender {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQSender.class);

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String virtualHost;
    private final String exchangeName;
    private final String routingKeyPrefix;

    RabbitMQSender(Map<String, String> config) {
        host = config.get(RabbitMQParamsConstants.NAME_RABBIT_MQ_HOST);
        port = Integer.parseInt(config.get(RabbitMQParamsConstants.NAME_RABBIT_MQ_PORT));
        username = config.get(RabbitMQParamsConstants.NAME_RABBIT_MQ_USERNAME);
        password = config.get(RabbitMQParamsConstants.NAME_RABBIT_MQ_PASSWORD);
        virtualHost = config.get(RabbitMQParamsConstants.NAME_RABBIT_MQ_VIRTUAL_HOST);
        exchangeName = config.get(RabbitMQParamsConstants.NAME_RABBIT_MQ_EXCHANGE_NAME);
        routingKeyPrefix = config.get(RabbitMQParamsConstants.NAME_RABBIT_MQ_ROUTING_KEY_PREFIX);
    }

    public AlertResult syncProduceMsg(String title, String content) {
        AlertResult alertResult = new AlertResult();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            RabbitMQMessageEntity messageEntity = generateMsgFromContent(title, content);
            channel.basicPublish(exchangeName, messageEntity.getRoutingKey(), MessageProperties.PERSISTENT_TEXT_PLAIN, messageEntity.getBody());
            alertResult.setStatus("true");
            alertResult.setMessage("rabbitmq发送成功");
        } catch (Exception e) {
            logger.error("send alert msg to rabbitMQ failed", e);
            alertResult.setStatus("false");
            alertResult.setMessage(String.format("send alert msg to rabbittMQ failed: %s", e.getMessage()));
        }
        return alertResult;
    }

    private RabbitMQMessageEntity generateMsgFromContent(String title, String content) {
        String key;
        if (StringUtils.isEmpty(routingKeyPrefix)){
            key = "";
        }else {
            key = routingKeyPrefix + ".";
        }
        if (title.toLowerCase().contains("query result")) {
            // sql task query result
            key += title;
        } else {
            // process instance execution status
            JsonNode processAlertContent = JSONUtils.parseArray(content).get(0);
            String processInstanceId = processAlertContent.get("processId").asText();
            String processInstanceName = processAlertContent.get("processName").asText();
            if (title.toLowerCase().contains("[start]")) {
                // process instance starts
                key += String.format("%s-%s-execution %s", processInstanceId, processInstanceName, "start");
            } else {
                // process instance ends
                key += String.format("%s-%s-execution %s", processInstanceId, processInstanceName, title.toLowerCase().contains("success") ? "success" : "failed");
            }
        }
        return new RabbitMQMessageEntity(key, content.getBytes(StandardCharsets.UTF_8));
    }

}
