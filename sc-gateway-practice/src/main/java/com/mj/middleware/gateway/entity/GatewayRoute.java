package com.mj.middleware.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 网关路由配置实体 — 练习动态路由
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRoute {

    /** 路由 ID */
    private String routeId;

    /** 目标 URI (如 lb://nacos-practice) */
    private String uri;

    /** 路径断言 (如 /api/user/**) */
    private String path;

    /** 路由谓词列表 */
    private String[] predicates;

    /** 过滤器列表 */
    private String[] filters;

    /** 顺序 (数字越小优先级越高) */
    private Integer order;

    /** 元数据 */
    private Map<String, String> metadata;

    /** 是否启用 */
    private Boolean enabled;
}
