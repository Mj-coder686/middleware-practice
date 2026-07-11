package com.mj.middleware.redis.service;

import com.mj.middleware.redis.entity.SignRecord;

/**
 * 签到业务接口
 */
public interface ISignService {

    /** 用户签到 */
    void sign(Long userId);

    /** 查询本月签到记录 */
    SignRecord getSignRecord(Long userId, String month);

    /** 统计连续签到天数 */
    int getConsecutiveDays(Long userId);

    /** 领取连续签到奖励 */
    void claimReward(Long userId);

    /** 统计本月签到总人数（HyperLogLog） */
    long countUniqueSignUsers(String month);
}
