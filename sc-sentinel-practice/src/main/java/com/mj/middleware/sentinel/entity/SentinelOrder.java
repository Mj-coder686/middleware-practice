package com.mj.middleware.sentinel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单实体 — 用于演示 Sentinel 熔断降级场景
 * (下单接口 → 调用库存服务/支付服务 → 服务慢或挂掉 → 熔断降级)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentinelOrder {

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 商品名称 */
    private String productName;

    /** 数量 */
    private Integer quantity;

    /** 总金额 */
    private Double totalAmount;

    /** 状态: PENDING/PAID/CANCELLED/FAILED */
    private String status;

    /** 备注 (降级时填写降级信息) */
    private String remark;
}
