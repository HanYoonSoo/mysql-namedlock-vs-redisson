package com.lock.mysql_namedlock_vs_redisson.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private Integer port;

    @Value("${redis.password:}")
    private String password;

    @Value("${redis.database:0}")
    private Integer database;

    @Value("${redis.ssl-enabled:false}")
    private boolean sslEnabled;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String protocol = sslEnabled ? "rediss://" : "redis://";

        config.useSingleServer()
                .setAddress(protocol + host + ":" + port)
                .setPassword(toRedissonPassword())
                .setDatabase(database);

        return Redisson.create(config);
    }

    private String toRedissonPassword() {
        if (password == null || password.isBlank()) {
            return null;
        }
        return password;
    }
}
