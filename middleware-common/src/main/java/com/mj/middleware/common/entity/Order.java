package com.mj.middleware.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体 — RabbitMQ/延迟队列 示例
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Long userId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer status;  // 0-待支付 1-已支付 2-已取消 3-已完成
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
