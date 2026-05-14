package com.lock.mysql_namedlock_vs_redisson;

import com.lock.mysql_namedlock_vs_redisson.lock.UserLevelLockFinal;
import com.lock.mysql_namedlock_vs_redisson.lock.UserLevelLockFinalWithDefaultDataSource;
import com.lock.mysql_namedlock_vs_redisson.lock.UserLevelLockWithRedisson;
import com.lock.mysql_namedlock_vs_redisson.lock.UserLevelLockWithJdbcTemplate;
import com.zaxxer.hikari.HikariDataSource;
import org.redisson.api.RedissonClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@SpringBootApplication
public class MysqlNamedlockVsRedissonApplication {

    public static void main(String[] args) {
        SpringApplication.run(MysqlNamedlockVsRedissonApplication.class, args);
    }

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties("userlock.datasource.hikari")
    public HikariDataSource userLockDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.clean();
            flyway.baseline();
            flyway.migrate();
        };
    }

    @Bean
    public UserLevelLockWithJdbcTemplate userLevelLockWithJdbcTemplate() {
        return new UserLevelLockWithJdbcTemplate(new NamedParameterJdbcTemplate(userLockDataSource()));
    }

    @Bean
    public UserLevelLockFinal userLevelLockFinal() {
        return new UserLevelLockFinal(userLockDataSource());
    }

    @Bean
    public UserLevelLockFinalWithDefaultDataSource userLevelLockFinalWithDefaultDataSource(HikariDataSource dataSource) {
        return new UserLevelLockFinalWithDefaultDataSource(dataSource);
    }

    @Bean
    public UserLevelLockWithRedisson userLevelLockWithRedisson(RedissonClient redissonClient) {
        return new UserLevelLockWithRedisson(redissonClient);
    }
}
