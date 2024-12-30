package com.palettee.global.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.palettee.chat.controller.dto.response.ChatResponse;
import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.global.redis.sub.RedisSubscriber;
import com.palettee.portfolio.controller.dto.response.PortFolioResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    public static final String CHAT_TOPIC = "ChatTopic";

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.password}")
    private String password;


    @Bean // Redis 서버와 연결
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
        redisConfiguration.setHostName(host);
        redisConfiguration.setPort(port);
        redisConfiguration.setPassword(password);
        return new LettuceConnectionFactory(redisConfiguration);
    }

    @Bean // Redis Pub/Sub에서 메시지가 교환되는 채널
    public ChannelTopic channelTopic(){
        return new ChannelTopic(CHAT_TOPIC);
    }

    @Bean // Redis Pub/Sub 메시지를 처리하는 리스너를 설정
    public MessageListenerAdapter listenerAdapter(RedisSubscriber redisSubscriber){
        // RedisSubscriber 객체를 연결하고 메시지 처리 메서드로 sendMessage를 지정
        return new MessageListenerAdapter(redisSubscriber,"sendMessage");
    }

    @Bean // Redis에서 메시지를 구독하고 감지된 메시지를 리스너로 전달하는 컨테이너
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter,
            ChannelTopic channelTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory); // Redis와 연결 정보를 설정
        container.addMessageListener(listenerAdapter,channelTopic); // Redis Pub/Sub 채널에서 메시지를 감지하면 리스너로 전달하도록 리스너를 등록
        return container;
    }

    @Bean
    public RedisTemplate<String, Long> redisTemplates(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Long.class)); // 조회수가 Long 타입
        return template;
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean("chatPubTemplate")
    public RedisTemplate<String, ChatResponse> chatPubTemplate(RedisConnectionFactory connectionFactory,
                                                                 @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        RedisTemplate<String, ChatResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    @Bean("chatRedisTemplate")
    public RedisTemplate<String, ChatResponse> chatRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ChatResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, GatheringResponse> RedisGatheringTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, GatheringResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, PortFolioResponse> RedisPortFolioTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, PortFolioResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }



    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofHours(3));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }

    @Bean(name = "redisObjectMapper") // LocalDateTime 직렬화 할 때 오류 발생 -> jsr310 Module 추가
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
