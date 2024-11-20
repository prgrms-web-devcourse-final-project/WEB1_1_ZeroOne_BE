package com.palettee.global.configs;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.*;

@Configuration
public class RedisConfig {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;
//
//    @Value("${redis.password}")
//    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
        redisConfiguration.setHostName(host);
        redisConfiguration.setPort(port);
//        redisConfiguration.setPassword(password);
        return new LettuceConnectionFactory(redisConfiguration);
    }

}
