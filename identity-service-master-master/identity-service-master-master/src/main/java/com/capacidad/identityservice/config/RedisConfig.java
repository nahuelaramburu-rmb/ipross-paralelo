package com.capacidad.identityservice.config;

import com.capacidad.identityservice.misc.ApplicationProperties;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

    @Bean(destroyMethod = "shutdown")
    ClientResources defaultClientResources() {
        return DefaultClientResources.create();
    }

    @Bean
    public ClientOptions clientOptions() {
        return ClientOptions.builder()
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .autoReconnect(true)
                .build();
    }

    @Bean
    LettucePoolingClientConfiguration lettucePoolConfig(ClientOptions options, @Qualifier("defaultClientResources") ClientResources dcr) {
        return LettucePoolingClientConfiguration.builder()
                .poolConfig(new GenericObjectPoolConfig<>())
                .clientOptions(options)
                .clientResources(dcr)
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build();
    }

    @Bean
    LettuceConnectionFactory redisConnectionFactory(LettucePoolingClientConfiguration clientConfiguration,
                                                    ApplicationProperties applicationProperties) {
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(applicationProperties.getRedisClusterNodes());
        redisClusterConfiguration.setMaxRedirects(applicationProperties.getRedisClusterMaxRedirects());
        return new LettuceConnectionFactory(redisClusterConfiguration, clientConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
