package com.mj.middleware.redis.controller;

import com.mj.middleware.common.result.Result;
import com.mj.middleware.redis.entity.Voucher;
import com.mj.middleware.redis.service.IVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 优惠券接口 — 购买 & 秒杀抢券
 */
@Tag(name = "优惠券管理", description = "购买优惠券 & 秒杀抢券")
@RestController
@RequestMapping("/voucher")
@RequiredArgsConstructor
public class VoucherController {

    private final IVoucherService voucherService;

    // ==================== 优惠券管理 ====================

    @Operation(summary = "新增优惠券")
    @PostMapping
    public Result<Void> addVoucher(@RequestBody Voucher voucher) {
        voucherService.add(voucher);
        return Result.success();
    }

    @Operation(summary = "查询优惠券详情")
    @GetMapping("/{id}")
    public Result<Voucher> getVoucher(@PathVariable Long id) throws InterruptedException {
        return Result.success(voucherService.getById(id));
    }

    // ==================== 普通购买 ====================

    @Operation(summary = "购买普通优惠券")
    @PostMapping("/buy/{voucherId}/{userId}")
    public Result<Long> buyVoucher(@PathVariable Long voucherId, @PathVariable Long userId) {
        return Result.success(voucherService.buyVoucher(voucherId, userId));
    }

    // ==================== 秒杀抢券 ====================

    @Operation(summary = "秒杀抢券（Lua 脚本原子操作）")
    @PostMapping("/seckill/{voucherId}/{userId}")
    public Result<Long> seckillVoucher(@PathVariable Long voucherId, @PathVariable Long userId) {
        return Result.success(voucherService.seckillVoucher(voucherId, userId));
    }

    @Operation(summary = "查询秒杀库存")
    @GetMapping("/seckill/{voucherId}/stock")
    public Result<Integer> getSeckillStock(@PathVariable Long voucherId) {
        return Result.success(voucherService.getSeckillStock(voucherId));
    }
}
