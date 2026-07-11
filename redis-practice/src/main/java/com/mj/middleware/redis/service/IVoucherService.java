package com.mj.middleware.redis.service;

import com.mj.middleware.redis.entity.Voucher;

/**
 * 优惠券业务接口
 */
public interface IVoucherService {

    /** 新增优惠券 */
    void add(Voucher voucher);

    /** 查询优惠券详情 */
    Voucher getById(Long id);

    /** 购买普通优惠券 */
    Long buyVoucher(Long voucherId, Long userId);

    /** 秒杀抢券（Lua 脚本） */
    Long seckillVoucher(Long voucherId, Long userId);

    /** 查询秒杀库存 */
    int getSeckillStock(Long voucherId);
}
