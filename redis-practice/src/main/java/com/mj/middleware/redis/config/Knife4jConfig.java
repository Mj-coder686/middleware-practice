package com.mj.middleware.redis.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Redis 练习模块 API")
                        .description("演示 Redis 五大数据类型、分布式锁、缓存策略等核心用法")
                        .version("1.0.0"));
    }
}
