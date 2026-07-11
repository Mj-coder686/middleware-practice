package com.mj.middleware.redis.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 签到数据访问 — Bitmap / HyperLogLog 底层操作
 *
 * key 设计：
 *   sign:{userId}:{yyyyMM}  → Bitmap（按天 offset）
 *   sign:count:{yyyyMM}     → HyperLogLog（签到人数去重统计）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SignMapper {

    private final StringRedisTemplate stringRedisTemplate;

    // TODO: void sign(Long userId, int dayOffset)
    // TODO: Boolean hasSigned(Long userId, String month, int dayOffset)
    // TODO: Long countSignDays(Long userId, String month)
    // TODO: void addUserToHll(String month, Long userId)
    // TODO: Long countHll(String month)
}
