package com.example.zxds.snm.task;

import com.example.zxds.snm.entity.NewOrderMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.GetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class NewOrderMessageTask {

    // Spring Boot 自动配置的 RabbitTemplate 实例，用于发送和接收消息
    private final RabbitTemplate rabbitTemplate;
    // Spring AMQP 管理接口，用于管理交换机、队列等
    private final AmqpAdmin amqpAdmin;
    // 交换机名称，所有新订单消息都通过此交换机路由
    private static final String EXCHANGE_NAME = "new_order_private";
    // 消息过期时间：1分钟（单位：毫秒）
    private static final int TTL = 60 * 1000;
    // JSON 转换器，用于消息体的序列化和反序列化（复用同一个实例避免性能开销）
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 同步发送新订单消息（注意：此方法不会创建队列，不会绑定路由键。测试时，先执行接收消息的方法。）
     * @param list 新订单消息列表
     */
    public void sendNewOrderMessage(List<NewOrderMessage> list) {
        for (NewOrderMessage message : list) {
            // convertAndSend：将消息对象自动转换为 JSON 后发送
            // 参数1：交换机名称
            // 参数2：路由键（使用 userId，实现消息路由到对应用户的队列）
            // 参数3：消息对象
            // 参数4：消息处理器，用于设置消息属性（这里设置过期时间）
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, message.getUserId(), message, msg -> {
                msg.getMessageProperties().setExpiration(String.valueOf(TTL));
                return msg;
            });
            log.info("发送成功: {}", message.getOrderId());
        }
    }

    /**
     * 异步发送新订单消息
     * @param list 新订单消息列表
     */
    @Async  // Spring 异步执行，不阻塞主线程
    public void sendNewOrderMessageAsync(List<NewOrderMessage> list) {
        sendNewOrderMessage(list);
    }

    /**
     * 同步接收新订单消息（轮询方式）
     * @param userId 司机用户ID
     * @return 新订单消息列表（按时间倒序，新消息在前）
     */
    public List<NewOrderMessage> receiveNewOrderMessage(long userId) {
        String queueName = "queue_" + userId;
        List<NewOrderMessage> list = new ArrayList<>();

        // execute：在 RabbitMQ Channel 中执行操作，自动管理连接和通道
        return rabbitTemplate.execute(channel -> {
            // 声明队列（如果不存在则自动创建）
            // 参数：队列名，持久化，不独占，不自动删除，无额外参数
            channel.queueDeclare(queueName, true, false, false, null);
            // 将队列绑定到交换机，使用 userId 作为路由键
            channel.queueBind(queueName, EXCHANGE_NAME, String.valueOf(userId));

            GetResponse response;
            // basicGet：非阻塞获取消息，如果没有消息立即返回 null
            while ((response = channel.basicGet(queueName, false)) != null) {
                // 从消息体反序列化为 NewOrderMessage 对象
                NewOrderMessage msg = objectMapper.readValue(response.getBody(), NewOrderMessage.class);
                list.add(msg);
                // 手动确认消息（参数2：false 表示只确认当前消息）
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            }

            // 翻转列表，使新消息排在前面
            Collections.reverse(list);
            return list;
        });
    }

    /**
     * 同步清空队列（司机退出系统时清空队列）
     * @param userId 司机用户ID
     */
    public void clearNewOrderQueue(long userId) {
        String queueName = "queue_" + userId;
        rabbitTemplate.execute(channel -> {
            // basicGet 第二个参数为 true 表示自动确认，消息被取出后立即删除
            while (channel.basicGet(queueName, true) != null) {
                // 循环直到队列为空
            }
            log.info("队列清空: {}", queueName);
            return null;
        });
    }

    /**
     * 异步清空队列（司机退出系统时清空队列）
     * @param userId 司机用户ID
     */
    @Async
    public void clearNewOrderQueueAsync(long userId) {
        clearNewOrderQueue(userId);
    }

    /**
     * 同步删除整个队列（司机注销时删除队列）
     * @param userId 司机用户ID
     */
    public void deleteNewOrderQueue(long userId) {
        amqpAdmin.deleteQueue("queue_" + userId);
    }

    /**
     * 异步删除整个队列（司机注销时删除队列）
     * @param userId 司机用户ID
     */
    @Async
    public void deleteNewOrderQueueAsync(long userId) {
        deleteNewOrderQueue(userId);
    }
}