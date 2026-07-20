package com.mj.middleware.nacos.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Nacos 配置中心注入的自定义属性
 * 对应 nacos-provider.yaml 中的 nacos.provider.* 配置
 * @RefreshScope 支持 Nacos 动态刷新
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = "nacos.provider")
public class NacosConfig {

    /** 应用名称 */
    private String appName;

    /** 运行环境 */
    private String env;

    /** 数据库描述 */
    private String dbDesc;

    /** Redis 描述 */
    private String redisDesc;
}
