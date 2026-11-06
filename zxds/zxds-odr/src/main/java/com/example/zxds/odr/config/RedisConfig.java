package com.example.zxds.odr.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import javax.annotation.Resource;

@Configuration
@ConditionalOnClass(RedisTemplate.class)  // 只有存在RedisTemplate时才生效
public class RedisConfig {

    @Bean
    @ConditionalOnMissingBean  // 没有配置时才生效
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * 定义Redis键过期事件的监听频道
     * 格式：__keyevent@<db>__:<event>
     * @5: 只监听5号数据库，实现业务隔离
     * expired: 监听键过期事件
     * Redis会在键过期时自动向此频道发布消息
     * 创建一个频道，如果key过期，redis服务器会作为消息的发布者，通过这个频道发布消息。那么如果有订阅者（监听器），这些订阅者就能收到消息。
     */
    @Bean
    public ChannelTopic expiredTopic() {
        return new ChannelTopic("__keyevent@5__:expired");
    }

    /**
     * 创建Redis消息监听容器
     * 负责管理Redis消息监听器：
     * 1. 维护Redis连接
     * 2. 接收指定频道的消息
     * 3. 将消息分发给注册的监听器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        return redisMessageListenerContainer;
    }
}