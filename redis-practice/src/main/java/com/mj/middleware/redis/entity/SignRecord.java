package com.mj.middleware.redis.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 签到记录 — 练习 Bitmap / HyperLogLog
 *
 * 设计思路：
 *   key = sign:{userId}:{年月}   → Redis Bitmap（按月，每天 1 bit）
 *   奖励：连续签到 7/14/28 天可领取积分
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "签到记录")
public class SignRecord {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "签到日期")
    private LocalDate signDate;

    @Schema(description = "连续签到天数")
    private Integer consecutiveDays;

    @Schema(description = "本月累计签到天数")
    private Integer totalDaysThisMonth;

    @Schema(description = "是否领取了连续签到奖励")
    private Boolean rewardClaimed;
}
