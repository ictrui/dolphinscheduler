package org.apache.dolphinscheduler.plugin.alert.rabbitmq;

import java.util.Map;
import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertInfo;
import org.apache.dolphinscheduler.alert.api.AlertResult;

public final class RabbitMQAlertChannel implements AlertChannel {

    @Override
    public AlertResult process(AlertInfo alertInfo) {
        AlertData alertData = alertInfo.getAlertData();
        Map<String, String> paramsMap = alertInfo.getAlertParams();
        if (null == paramsMap) {
            return new AlertResult("false", "rabbitmq params is null");
        }
        return new RabbitMQSender(paramsMap).syncProduceMsg(alertData.getTitle(), alertData.getContent());
    }
}
