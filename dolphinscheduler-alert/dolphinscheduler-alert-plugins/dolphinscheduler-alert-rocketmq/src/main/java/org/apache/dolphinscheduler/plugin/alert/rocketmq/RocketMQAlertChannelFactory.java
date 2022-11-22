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

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class RocketMQAlertChannelFactory implements AlertChannelFactory {
    @Override
    public String name() {
        return "RocketMQ";
    }

    @Override
    public List<PluginParams> params() {
        InputParam producerGroupNameParam = InputParam
                .newBuilder(RocketMQParamsConstants.NAME_ROCKET_MQ_PRODUCER_GROUP_NAME, RocketMQParamsConstants.NAME_ROCKET_MQ_PRODUCER_GROUP_NAME)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();
        InputParam  nameServerAddrParam = InputParam
                .newBuilder(RocketMQParamsConstants.NAME_ROCKET_MQ_NAME_SERVER_ADDRESS, RocketMQParamsConstants.ROCKET_MQ_NAME_SERVER_ADDRESS)
                .setPlaceholder("separated by a semicolon if there is more than one NameServer, such as \"127.0.0.2:9876;127.0.0.3:9876\"")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam topicParam = InputParam
                .newBuilder(RocketMQParamsConstants.NAME_ROCKET_MQ_TOPIC, RocketMQParamsConstants.ROCKET_MQ_TOPIC)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam tagsParam = InputParam
                .newBuilder(RocketMQParamsConstants.NAME_ROCKET_MQ_TAGS, RocketMQParamsConstants.ROCKET_MQ_TAGS)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        return Arrays.asList(producerGroupNameParam, nameServerAddrParam, topicParam, tagsParam);
    }

    @Override
    public AlertChannel create() {
        return new RocketMQAlertChannel();
    }
}
