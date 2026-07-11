package com.mj.middleware.redis.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 优惠券数据访问 — String（库存）+ Set（已购用户）底层操作
 *
 * key 设计：
 *   seckill:stock:{voucherId}       → String（秒杀库存）
 *   seckill:ordered:{voucherId}     → Set（已下单用户集合）
 *   global:voucherId                → String（优惠券 ID 自增器）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class VoucherMapper {

    private final StringRedisTemplate stringRedisTemplate;

    // TODO: void initSeckillStock(Long voucherId, int stock)
    // TODO: int getSeckillStock(Long voucherId)
    // TODO: Long decrStock(Long voucherId)
    // TODO: boolean hasOrdered(Long voucherId, Long userId)
    // TODO: void addOrderedUser(Long voucherId, Long userId)
    // TODO: Long seckillByLua(Long voucherId, Long userId)
    // TODO: Long nextVoucherId()
}
