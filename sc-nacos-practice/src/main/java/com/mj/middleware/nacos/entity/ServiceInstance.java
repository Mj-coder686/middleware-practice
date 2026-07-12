package com.mj.middleware.nacos.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 服务实例信息 — 练习服务发现
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInstance {

    /** 实例 IP */
    private String ip;

    /** 实例端口 */
    private int port;

    /** 服务名 */
    private String serviceName;

    /** 权重 (Nacos 权重路由) */
    private double weight;

    /** 是否健康 */
    private boolean healthy;

    /** 元数据 */
    private String metadata;

    /** 上线时间 */
    private LocalDateTime registerTime;
}
