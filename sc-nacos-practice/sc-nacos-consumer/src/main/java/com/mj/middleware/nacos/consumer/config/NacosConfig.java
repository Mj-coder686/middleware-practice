package com.mj.middleware.nacos.consumer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Nacos 配置中心注入的自定义属性
 * 对应 nacos-consumer.yaml 中的 nacos.consumer.* 配置
 * @RefreshScope 支持 Nacos 动态刷新
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = "nacos.consumer")
public class NacosConfig {

    /** 应用名称 */
    private String appName;

    /** 运行环境 */
    private String env;

    /** 功能开关 */
    private Boolean featureEnabled;

    /** 欢迎消息 — 可在 Nacos 控制台动态修改来验证刷新 */
    private String welcomeMessage;

    /** 负载均衡策略 */
    private String loadBalancer;
}
