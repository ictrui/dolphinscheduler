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

public final class RocketMQParamsConstants {
    static final String ROCKET_MQ_PRODUCER_GROUP_NAME = "$t('producerGroupName')";
    static final String NAME_ROCKET_MQ_PRODUCER_GROUP_NAME = "producerGroupName";
    static final String ROCKET_MQ_NAME_SERVER_ADDRESS = "$t('namesrvAddr')";
    static final String NAME_ROCKET_MQ_NAME_SERVER_ADDRESS = "namesrvAddr";
    static final String ROCKET_MQ_TOPIC = "$t('topic')";
    static final String NAME_ROCKET_MQ_TOPIC = "topic";
    static final String ROCKET_MQ_TAGS = "$t('tags')";
    static final String NAME_ROCKET_MQ_TAGS = "tags";

    private RocketMQParamsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
