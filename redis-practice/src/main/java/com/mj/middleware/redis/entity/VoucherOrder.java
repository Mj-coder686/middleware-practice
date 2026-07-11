package com.mj.middleware.redis.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券订单 — 练习下单扣库存
 *
 * 普通下单流程：
 *   1. 判断库存是否充足
 *   2. 判断用户是否已达限购上限
 *   3. 扣减库存（DECRBY）
 *   4. 生成订单
 *
 * 秒杀下单流程（Lua 脚本原子化）：
 *   1. 判断库存 > 0
 *   2. 判断用户不在已购集合中
 *   3. 扣减库存 + 加入已购集合（原子）
 *   4. 进入阻塞队列，异步创建订单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "优惠券订单")
public class VoucherOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单 ID")
    private Long id;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "优惠券 ID")
    private Long voucherId;

    @Schema(description = "下单时的秒杀价")
    private BigDecimal payPrice;

    @Schema(description = "订单状态：0-未支付 1-已支付 2-已核销 3-已取消")
    private Integer status;

    @Schema(description = "下单时间")
    private LocalDateTime createTime;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "核销时间")
    private LocalDateTime useTime;
}
