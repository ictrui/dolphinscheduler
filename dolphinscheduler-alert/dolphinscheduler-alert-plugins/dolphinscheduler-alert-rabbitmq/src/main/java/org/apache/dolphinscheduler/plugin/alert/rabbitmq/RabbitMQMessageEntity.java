package org.apache.dolphinscheduler.plugin.alert.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RabbitMQMessageEntity {
    private String routingKey;
    private byte[] body;
}
