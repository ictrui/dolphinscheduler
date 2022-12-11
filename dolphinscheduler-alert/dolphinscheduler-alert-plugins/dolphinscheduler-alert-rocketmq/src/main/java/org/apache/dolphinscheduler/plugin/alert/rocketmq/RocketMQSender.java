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

package org.apache.dolphinscheduler.plugin.alert.rocketmq;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class RocketMQSender {

    private static final Logger logger = LoggerFactory.getLogger(RocketMQSender.class);

    private final String producerGroupName;
    private final String nameSrvAddress;
    private final String topic;
    private final String tags;

    RocketMQSender(Map<String, String> config){
        producerGroupName = config.get(RocketMQParamsConstants.NAME_ROCKET_MQ_PRODUCER_GROUP_NAME);
        nameSrvAddress = config.get(RocketMQParamsConstants.NAME_ROCKET_MQ_NAME_SERVER_ADDRESS);
        topic = config.get(RocketMQParamsConstants.NAME_ROCKET_MQ_TOPIC);
        tags = config.get(RocketMQParamsConstants.NAME_ROCKET_MQ_TAGS);
    }

    public AlertResult syncProduceMsg(String title, String content){
        AlertResult alertResult = new AlertResult();
        DefaultMQProducer producer = null;
        try {
            producer = new DefaultMQProducer(producerGroupName);
            producer.setNamesrvAddr(nameSrvAddress);
            producer.start();
            SendResult sendResult = producer.send(generateMsgFromContent(title, content));
            alertResult.setStatus("true");
            alertResult.setMessage(sendResult.toString());
        }catch (Exception e){
            logger.error("send alert msg to rocketMQ failed", e);
            alertResult.setStatus("false");
            alertResult.setMessage(String.format("send alert msg to rocketMQ failed: %s", e.getMessage()));
        }finally {
            if (null != producer){
                producer.shutdown();
            }
        }
        return alertResult;
    }

    private Message generateMsgFromContent(String title, String content){
        String key;
        if (title.toLowerCase().contains("query result")){
            // sql task query result
            key = title;
        } else {
            // process instance execution status
            JsonNode processAlertContent = JSONUtils.parseArray(content).get(0);
            String processInstanceId = processAlertContent.get("processId").asText();
            String processInstanceName = processAlertContent.get("processName").asText();
            if (title.toLowerCase().contains("[start]")){
                // process instance starts
                key = String.format("%s-%s-execution %s", processInstanceId, processInstanceName, "start");
            }else {
                // process instance ends
                key = String.format("%s-%s-execution %s", processInstanceId, processInstanceName, title.toLowerCase().contains("success") ? "success" : "failed");
            }
        }
        return new Message(topic, tags, key, content.getBytes(StandardCharsets.UTF_8));
    }

}
