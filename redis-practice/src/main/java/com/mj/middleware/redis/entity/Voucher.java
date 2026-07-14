package com.mj.middleware.redis.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券 — 练习购买 & 秒杀抢券
 *
 * 普通券：直接 Redis DECRLBY 库存 + 订单
 * 秒杀券：Lua 脚本原子校验库存 & 用户是否已购买 & 扣减
 *   key = seckill:stock:{voucherId}       String（库存数量）
 *   key = seckill:ordered:{voucherId}     Set（已下单用户）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "优惠券")
public class Voucher implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "优惠券 ID")
    private Long id;

    @Schema(description = "优惠券标题")
    private String title;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "折扣价 / 秒杀价")
    private BigDecimal discountPrice;

    @Schema(description = "库存数量")
    private Integer stock;

    @Schema(description = "每人限购数量")
    private Integer perLimit;

    @Schema(description = "类型：NORMAL-普通券 / SECKILL-秒杀券")
    private String type;

    @Schema(description = "使用开始时间")
    private LocalDateTime beginTime;

    @Schema(description = "使用结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态：0-未开始 1-进行中 2-已结束")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
