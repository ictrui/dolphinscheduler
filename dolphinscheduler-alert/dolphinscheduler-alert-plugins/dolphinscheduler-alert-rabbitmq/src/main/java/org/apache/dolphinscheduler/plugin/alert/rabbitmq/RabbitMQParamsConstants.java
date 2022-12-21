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

public final class RabbitMQParamsConstants {
    static final String RABBIT_MQ_HOST = "$t('host')";
    static final String RABBIT_MQ_PORT = "$t('port')";
    static final String RABBIT_MQ_USERNAME = "$t('username')";
    static final String RABBIT_MQ_PASSWORD = "$t('password')";
    static final String RABBIT_MQ_VIRTUAL_HOST = "$t('virtualHost')";
    static final String RABBIT_MQ_EXCHANGE_NAME = "$t('exchangeName')";
    static final String RABBIT_MQ_ROUTING_KEY_PREFIX = "$t('routingKeyPrefix')";
    static final String NAME_RABBIT_MQ_HOST = "host";
    static final String NAME_RABBIT_MQ_PORT = "port";
    static final String NAME_RABBIT_MQ_USERNAME = "username";
    static final String NAME_RABBIT_MQ_PASSWORD = "password";
    static final String NAME_RABBIT_MQ_VIRTUAL_HOST = "virtualHost";
    static final String NAME_RABBIT_MQ_EXCHANGE_NAME = "exchangeName";
    static final String NAME_RABBIT_MQ_ROUTING_KEY_PREFIX = "routingKeyPrefix";

    private RabbitMQParamsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
