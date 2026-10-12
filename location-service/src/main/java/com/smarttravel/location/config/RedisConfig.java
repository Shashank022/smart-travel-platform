package com.smarttravel.location.config;

import com.smarttravel.location.dto.LocationResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, LocationResponse> locationRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {

        StringRedisSerializer keySerializer =
                new StringRedisSerializer();

        JacksonJsonRedisSerializer<LocationResponse> valueSerializer =
                new JacksonJsonRedisSerializer<>(LocationResponse.class);

        RedisSerializationContext<String, LocationResponse> context =
                RedisSerializationContext.<String, LocationResponse>newSerializationContext(keySerializer)
                        .value(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(
                connectionFactory,
                context
        );
    }
}