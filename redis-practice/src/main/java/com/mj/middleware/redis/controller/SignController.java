package com.mj.middleware.redis.controller;

import com.mj.middleware.common.result.Result;
import com.mj.middleware.redis.entity.SignRecord;
import com.mj.middleware.redis.service.ISignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 签到接口 — Bitmap / HyperLogLog 练习
 */
@Tag(name = "签到管理", description = "每日签到 & 连续签到奖励")
@RestController
@RequestMapping("/sign")
@RequiredArgsConstructor
public class SignController {

    private final ISignService signService;

    @Operation(summary = "签到")
    @PostMapping("/{userId}")
    public Result<Void> sign(@PathVariable Long userId) {
        signService.sign(userId);
        return Result.success();
    }

    @Operation(summary = "查询本月签到记录（Bitmap）")
    @GetMapping("/{userId}/record")
    public Result<SignRecord> getSignRecord(
            @PathVariable Long userId,
            @Parameter(description = "查询月份，格式 yyyy-MM") @RequestParam(required = false) String month) {
        return Result.success(signService.getSignRecord(userId, month));
    }

    @Operation(summary = "统计本月连续签到天数")
    @GetMapping("/{userId}/consecutive")
    public Result<Integer> getConsecutiveDays(@PathVariable Long userId) {
        return Result.success(signService.getConsecutiveDays(userId));
    }

    @Operation(summary = "领取连续签到奖励")
    @PostMapping("/{userId}/reward")
    public Result<Void> claimReward(@PathVariable Long userId) {
        signService.claimReward(userId);
        return Result.success();
    }

    @Operation(summary = "统计本月签到总人数（HyperLogLog）")
    @GetMapping("/count")
    public Result<Long> countSignUsers(
            @Parameter(description = "查询月份，格式 yyyy-MM") @RequestParam(required = false) String month) {
        return Result.success(signService.countUniqueSignUsers(month));
    }
}
