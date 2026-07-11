package com.mj.middleware.mp.config;

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
                        .title("MyBatis-Plus 练习模块 API")
                        .description("演示 MyBatis-Plus CRUD、条件构造器、分页、逻辑删除、乐观锁等功能")
                        .version("1.0.0"));
    }
}
