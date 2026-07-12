package com.mj.middleware.nacos.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nacos 配置项 — 练习配置中心动态刷新
 *
 * 这些字段值来自 Nacos 配置中心，@RefreshScope 注解可以实现热更新
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NacosConfig {

    /** 应用名 */
    private String appName;

    /** 环境标识 */
    private String env;

    /** 自定义开关 */
    private Boolean featureEnabled;

    /** 自定义消息 */
    private String welcomeMessage;

    /** 数据库连接数 (模拟动态调整) */
    private Integer dbPoolSize;
}
