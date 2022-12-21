import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class SendToRabbitMQ {
    @Test
    public void sentMQ() throws Exception {
            AlertResult alertResult = new AlertResult();
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("xxx");
            factory.setPort(5672);
            factory.setVirtualHost("xxx");
            factory.setUsername("xxx");
            factory.setPassword("xxx");
            try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
                channel.basicPublish("xxx_exchange", "xxx", MessageProperties.PERSISTENT_TEXT_PLAIN, "发送测试".getBytes(StandardCharsets.UTF_8));
                alertResult.setStatus("true");
                alertResult.setMessage("rabbitmq发送成功");
                System.out.println("发送成功");
            } catch (Exception e) {
                System.out.println("send alert msg to rabbitMQ failed");
                throw e;
            }
    }
}
