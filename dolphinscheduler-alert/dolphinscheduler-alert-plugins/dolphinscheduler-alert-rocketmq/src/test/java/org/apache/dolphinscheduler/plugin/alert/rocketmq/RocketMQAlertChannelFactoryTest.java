package org.apache.dolphinscheduler.plugin.alert.rocketmq;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class RocketMQAlertChannelFactoryTest {

    @Test
    public void testGetParams() {
        RocketMQAlertChannelFactory rocketMQAlertChannelFactory = new RocketMQAlertChannelFactory();
        List<PluginParams> params = rocketMQAlertChannelFactory.params();
        JSONUtils.toJsonString(params);
        Assert.assertEquals(4, params.size());
    }

    @Test
    public void testCreate(){
        RocketMQAlertChannelFactory rocketMQAlertChannelFactory = new RocketMQAlertChannelFactory();
        AlertChannel alertChannel = rocketMQAlertChannelFactory.create();
        Assert.assertNotNull(alertChannel);
    }
}
