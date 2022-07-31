package com.hanghae.degether.websocket.config;

import com.hanghae.degether.sse.SseRedisSubscriber;
import com.hanghae.degether.websocket.service.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    String hostname;
    @Bean
    public ChannelTopic channelTopic() {
        return new ChannelTopic("project");
    }
    @Bean
    public ChannelTopic sseTopic() {
        return new ChannelTopic("sse");
    }

    // myredis 연결
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(hostname, 49155);
        redisStandaloneConfiguration.setPassword("redispw");
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }
    /**
     * redis pub/sub 메시지를 처리하는 listener 설정
     * redis.publist 할때 여기로 와서 container에 담음
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory,MessageListenerAdapter listenerAdapter,MessageListenerAdapter sseListenerAdapter, ChannelTopic channelTopic, ChannelTopic sseTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(sseListenerAdapter, sseTopic);
        container.addMessageListener(listenerAdapter, channelTopic);
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }
    @Bean
    public MessageListenerAdapter sseListenerAdapter(SseRedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }
    /*
     * redisTemplate
     * setKeySerializer, setValueSerializer 설정해주는 이유
     * RedisTemplate를 사용할 때 Spring - Redis 간 데이터 직렬화, 역직렬화 시 사용하는 방식이 Jdk 직렬화 방식이기 때문입니다
     * 직렬화와 역직렬화를 통해 redis-cli 를 이용해 데이터 값을 볼 수 있다.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //일반적인 Key:value 경우 serializer
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        //Hash 사용할 경우 serializer
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        //redis connectionFactory 사용
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }
    /*
     * String RedisTemplate 설정
     * 위에 선언한 RedisTemplate 보다 좀더 문자열에 특화된 Serialize 제공한다
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        final StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setKeySerializer(new StringRedisSerializer());
        stringRedisTemplate.setValueSerializer(new StringRedisSerializer());
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory());
        return stringRedisTemplate;
    }

}
