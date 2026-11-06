package com.example.zxds.snm.task;

import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.snm.db.pojo.MessageEntity;
import com.example.zxds.snm.db.pojo.MessageRefEntity;
import com.example.zxds.snm.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageTask {

    // 发送的消息都是字符串形式，采用字符串专用的 RabbitTemplate
    @Resource(name = "stringRabbitTemplate")
    private RabbitTemplate rabbitTemplate;

    private final MessageService messageService;

    /**
     * 同步发送私有消息
     */
    public void sendPrivateMessage(String identity, long userId, Integer ttl, MessageEntity entity) {
        try {
            // 将消息保存到MongoDB
            String id = messageService.insertMessage(entity);

            // 将消息发送到RabbitMQ队列中
            String exchangeName = identity + "_private"; // 以  身份标识（司机/乘客/系统） 作为交换机
            String routingKey = String.valueOf(userId); // 以userId作为路由键

            // 确保交换机和队列存在
            rabbitTemplate.execute(channel -> {
                // 声明交换机
                channel.exchangeDeclare(exchangeName, "direct", true);
                // 声明队列
                channel.queueDeclare("queue_" + userId, true, false, false, null);
                // 队列和交换机绑定路由键
                channel.queueBind("queue_" + userId, exchangeName, routingKey);
                return null;
            });

            Map<String, Object> headers = new HashMap<>();
            // MongoDB中保存到message集合中的文档的主键值放到消息头中。
            headers.put("messageId", id);

            rabbitTemplate.convertAndSend(exchangeName, routingKey, entity.getMsg(), message -> {
                message.getMessageProperties().getHeaders().putAll(headers);
                message.getMessageProperties().setContentEncoding("UTF-8");
                if (ttl != null && ttl > 0) {
                    message.getMessageProperties().setExpiration(ttl.toString());
                }
                return message;
            });

            log.debug("私有消息发送成功, userId: {}", userId);
        } catch (Exception e) {
            log.error("发送私有消息异常", e);
            throw new ZxdsException("向MQ发送消息失败");
        }
    }

    /**
     * 异步发送私有消息
     */
    @Async
    public void sendPrivateMessageAsync(String identity, long userId, Integer ttl, MessageEntity entity) {
        sendPrivateMessage(identity, userId, ttl, entity);
    }

    /**
     * 同步发送广播消息
     */
    public void sendBroadcastMessage(String identity, Integer ttl, MessageEntity entity) {
        try {
            // 保存到MongoDB
            String id = messageService.insertMessage(entity);

            // 广播消息
            String exchangeName = identity + "_broadcast";

            // 确保交换机存在（FANOUT类型）
            rabbitTemplate.execute(channel -> {
                channel.exchangeDeclare(exchangeName, "fanout", true);
                return null;
            });

            Map<String, Object> headers = new HashMap<>();
            headers.put("messageId", id);

            rabbitTemplate.convertAndSend(exchangeName, "", entity.getMsg(), message -> {
                message.getMessageProperties().getHeaders().putAll(headers);
                // 设置消息为持久化模式，确保RabbitMQ重启后消息不会丢失
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                message.getMessageProperties().setContentEncoding("UTF-8");
                if (ttl != null && ttl > 0) {
                    message.getMessageProperties().setExpiration(ttl.toString());
                }
                return message;
            });

            log.debug("广播消息发送成功");
        } catch (Exception e) {
            log.error("发送广播消息异常", e);
            throw new ZxdsException("向MQ发送消息失败");
        }
    }

    /**
     * 异步发送广播消息
     */
    @Async
    public void sendBroadcastMessageAsync(String identity, Integer ttl, MessageEntity entity) {
        sendBroadcastMessage(identity, ttl, entity);
    }

    /**
     * 同步接收消息
     */
    public int receive(String identity, long userId) {
        String queueName = "queue_" + userId;
        int count = 0;

        try {
            // 确保队列存在并绑定到私有交换机
            rabbitTemplate.execute(channel -> {
                // 声明交换机
                channel.exchangeDeclare(identity + "_private", "direct", true);
                // 声明队列
                channel.queueDeclare(queueName, true, false, false, null);
                // 队列和交换机 使用 路由键 绑定
                channel.queueBind(queueName, identity + "_private", String.valueOf(userId));

                // 声明广播的交换机
                channel.exchangeDeclare(identity + "_broadcast", "fanout", true);
                // 负责广播的交换机绑定队列
                channel.queueBind(queueName, identity + "_broadcast", "");
                return null;
            });

            // 接收私有消息
            count += receiveMessages(queueName, identity, userId, "私有");

            // 接收广播消息
            count += receiveMessages(queueName, identity, userId, "广播");

            return count;
        } catch (Exception e) {
            log.error("接收消息异常", e);
            throw new ZxdsException("接收消息失败");
        }
    }

    /**
     * 接收消息通用方法
     */
    private int receiveMessages(String queueName, String identity, long userId, String type) {
        int count = 0;

        while (true) {
            org.springframework.amqp.core.Message message = rabbitTemplate.receive(queueName);
            if (message == null) {
                break;
            }

            try {
                Map<String, Object> headers = message.getMessageProperties().getHeaders();
                String messageId = headers.get("messageId").toString();
                String content = new String(message.getBody());

                log.debug("从RabbitMQ接收的{}消息：{}", type, content);

                MessageRefEntity refEntity = new MessageRefEntity();
                refEntity.setMessageId(messageId);
                refEntity.setReceiverId(userId);
                refEntity.setReceiverIdentity(identity);
                refEntity.setReadFlag(false);
                refEntity.setLastFlag(true);

                // 接收消息后，将接收者的状态信息保存到MongoDB
                messageService.insertRef(refEntity);

                // 手动确认
                rabbitTemplate.execute(channel -> {
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    return null;
                });

                count++;
            } catch (Exception e) {
                log.error("处理消息异常", e);
                // 消息处理失败，拒绝并重新入队
                rabbitTemplate.execute(channel -> {
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                    return null;
                });
            }
        }

        return count;
    }

    /**
     * 异步接收消息
     */
    @Async
    public void receiveAsync(String identity, long userId) {
        receive(identity, userId);
    }

    /**
     * 同步删除消息队列
     */
    public void deleteQueue(String identity, long userId) {
        String queueName = "queue_" + userId;
        try {
            rabbitTemplate.execute(channel -> {
                channel.queueDelete(queueName);
                return null;
            });
            log.debug("消息队列删除成功: {}", queueName);
        } catch (Exception e) {
            log.error("删除队列失败", e);
            throw new ZxdsException("删除队列失败");
        }
    }

    /**
     * 异步删除消息队列
     */
    @Async
    public void deleteQueueAsync(String identity, long userId) {
        deleteQueue(identity, userId);
    }

    /**
     * 专门接收账单消息的方法（只接收一条消息，并且接收即表示已读）
     * @param identity
     * @param userId
     * @return
     */
    public String receiveBillMessage(String identity, long userId) {
        String exchangeName = identity + "_private";
        String queueName = "queue_" + userId;
        String routingKey = String.valueOf(userId);

        try {
            // 确保交换机和队列存在并绑定
            rabbitTemplate.execute(channel -> {
                channel.exchangeDeclare(exchangeName, "direct", true);
                channel.queueDeclare(queueName, true, false, false, null);
                channel.queueBind(queueName, exchangeName, routingKey);
                channel.basicQos(0, 1, true);
                return null;
            });

            // 接收一条消息
            org.springframework.amqp.core.Message message = rabbitTemplate.receive(queueName);
            if (message == null) {
                return "";
            }

            // 解析消息
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            String messageId = headers.get("messageId").toString();
            String msg = new String(message.getBody());

            log.debug("从RabbitMQ接收的订单消息：{}", msg);

            // 保存接收状态
            MessageRefEntity entity = new MessageRefEntity();
            entity.setMessageId(messageId);
            entity.setReceiverId(userId);
            entity.setReceiverIdentity(identity);
            entity.setReadFlag(true);
            entity.setLastFlag(true);
            messageService.insertRef(entity);

            // 手动确认
            rabbitTemplate.execute(channel -> {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return null;
            });

            return msg;
        } catch (Exception e) {
            log.error("接收订单消息异常", e);
            throw new ZxdsException("接收新订单失败");
        }
    }
}