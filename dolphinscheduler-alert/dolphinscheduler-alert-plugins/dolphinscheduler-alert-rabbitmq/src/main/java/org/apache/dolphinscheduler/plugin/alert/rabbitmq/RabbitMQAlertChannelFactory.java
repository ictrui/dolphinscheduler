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

import com.google.auto.service.AutoService;
import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.params.PasswordParam;
import org.apache.dolphinscheduler.spi.params.base.DataType;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;

import java.util.Arrays;
import java.util.List;

@AutoService(AlertChannelFactory.class)
public final class RabbitMQAlertChannelFactory implements AlertChannelFactory {
    @Override
    public String name() {
        return "RabbitMQ";
    }

    @Override
    public List<PluginParams> params() {
        InputParam hostNameParam = InputParam
                .newBuilder(RabbitMQParamsConstants.NAME_RABBIT_MQ_HOST, RabbitMQParamsConstants.NAME_RABBIT_MQ_HOST)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam portParam = InputParam
                .newBuilder(RabbitMQParamsConstants.NAME_RABBIT_MQ_PORT, RabbitMQParamsConstants.NAME_RABBIT_MQ_PORT)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam usernameParam = InputParam
                .newBuilder(RabbitMQParamsConstants.NAME_RABBIT_MQ_USERNAME, RabbitMQParamsConstants.NAME_RABBIT_MQ_USERNAME)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        PasswordParam passwordParam = PasswordParam
                .newBuilder(RabbitMQParamsConstants.NAME_RABBIT_MQ_PASSWORD, RabbitMQParamsConstants.NAME_RABBIT_MQ_PASSWORD)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();
        InputParam virtualHostParam = InputParam
                .newBuilder(RabbitMQParamsConstants.NAME_RABBIT_MQ_VIRTUAL_HOST, RabbitMQParamsConstants.NAME_RABBIT_MQ_VIRTUAL_HOST)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();
        InputParam exchangeNameParam = InputParam
                .newBuilder(RabbitMQParamsConstants.NAME_RABBIT_MQ_EXCHANGE_NAME, RabbitMQParamsConstants.NAME_RABBIT_MQ_EXCHANGE_NAME)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam routingKeyPrefixParam = InputParam
                .newBuilder(RabbitMQParamsConstants.NAME_RABBIT_MQ_ROUTING_KEY_PREFIX, RabbitMQParamsConstants.NAME_RABBIT_MQ_ROUTING_KEY_PREFIX)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        return Arrays.asList(hostNameParam, portParam, usernameParam, passwordParam, virtualHostParam, exchangeNameParam, routingKeyPrefixParam);
    }

    @Override
    public AlertChannel create() {
        return new RabbitMQAlertChannel();
    }
}
