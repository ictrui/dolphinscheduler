package org.apache.dolphinscheduler.plugin.alert.rocketmq;

import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RocketMQSenderTest {
    private static final Map<String, String> rocketMQConfig = new HashMap<>();

    @Before
    public void initRocketMQConfig(){
        rocketMQConfig.put(RocketMQParamsConstants.NAME_ROCKET_MQ_PRODUCER_GROUP_NAME, "testProducerGroup");
        rocketMQConfig.put(RocketMQParamsConstants.NAME_ROCKET_MQ_NAME_SERVER_ADDRESS, "172.16.0.8:9876");
        rocketMQConfig.put(RocketMQParamsConstants.NAME_ROCKET_MQ_TOPIC, "rulemgmt");
        rocketMQConfig.put(RocketMQParamsConstants.NAME_ROCKET_MQ_TAGS, "testTags");
    }

    @Test
    public void testSend(){
        RocketMQSender rocketMQSender = new RocketMQSender(rocketMQConfig);
        String title = "query result";
        String content = "{\"projectCode\":12345,\"projectName\":\"测试项目\",\"owner\":\"owner1\",\"processId\":121,\"processDefinitionCode\":123456,\"processName\":\"测试工作流-20221212111\"}";
        AlertResult alertResult = rocketMQSender.syncProduceMsg(title, content);
        Assert.assertEquals("false", alertResult.getStatus());
    }
}
