package com.example.zxds.odr.config;

import com.example.zxds.odr.db.dao.OrderBillDao;
import com.example.zxds.odr.db.dao.OrderDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Component
public class KeyExpiredListener extends KeyExpirationEventMessageListener {

    // 构造注入当前对象
    public KeyExpiredListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Resource
    private OrderDao orderDao;

    @Resource
    private OrderBillDao orderBillDao;

    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        // message 参数是Redis服务器通过频道发送过来的消息
        // 判断，如果这个消息是通过这个键国企频道发送过来的
        if(new String(message.getChannel()).equals("__keyevent@5__:expired")){
            //反序列化Key，否则出现乱码（如果使用的RedisTemplate，则需要编写以下两行代码进行反序列化）
            //JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer();
            //String key = serializer.deserialize(message.getBody()).toString();

            // 咱们使用的是 StringRedisTemplate
            String key = new String(message.getBody());

            // 如果 key 属于订单缓存
            if(key.contains("order#")){
                long orderId = Long.parseLong(key.split("#")[1]);
                int rows = orderDao.deleteUnAcceptOrder(Map.of("orderId", orderId));
                if(rows == 1){
                    log.info("删除了无人接单的订单：" + orderId);
                }
                rows = orderBillDao.deleteUnAcceptOrderBill(orderId);
                if(rows == 1){
                    log.info("删除了无人接单的账单：" + orderId);
                }
            }
        }
        super.onMessage(message, pattern);
    }
}
