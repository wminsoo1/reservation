package com.example.Reservation.config;

import com.example.Reservation.listener.CacheInvalidationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    @Bean
    public RedisScript<Long> issueCouponScript() {
        Resource scriptSource = new ClassPathResource("scripts/issue_coupon.lua");
        return RedisScript.of(scriptSource, Long.class);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // "cache-evict-topic" 이라는 방송 채널(Topic)을 항상 듣고 있게 합니다.
        container.addMessageListener(listenerAdapter, new PatternTopic("cache-evict-topic"));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(CacheInvalidationListener listener) {
        // 메시지가 들어오면 CacheInvalidationListener 클래스의 "onMessage" 메서드를 실행하라고 지정합니다.
        return new MessageListenerAdapter(listener, "onMessage");
    }

}