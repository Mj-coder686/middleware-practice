package com.mj.middleware.sentinel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 接口统计信息 — 用于 Sentinel 热点参数限流演示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiStats {

    /** 接口路径 */
    private String path;

    /** 总调用次数 */
    private Long totalCount;

    /** 通过次数 */
    private Long passedCount;

    /** 被限流次数 */
    private Long blockedCount;

    /** 参数值 (热点参数: userId=xxx) */
    private Map<String, String> params;

    /** 平均响应时间(ms) */
    private Long avgRt;

    /** 统计时间 */
    private LocalDateTime timestamp;
}
