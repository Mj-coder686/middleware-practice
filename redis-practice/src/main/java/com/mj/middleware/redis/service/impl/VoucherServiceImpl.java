package com.mj.middleware.redis.service.impl;

import com.mj.middleware.redis.entity.Voucher;
import com.mj.middleware.redis.mapper.VoucherMapper;
import com.mj.middleware.redis.service.IVoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 优惠券业务实现 — TODO: 使用 Redis String / Lua 脚本实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements IVoucherService {

    private final VoucherMapper voucherMapper;

    @Override
    public void add(Voucher voucher) {
        // TODO: 存储到 MySQL/Redis
        // TODO: 如果是秒杀券 → SET seckill:stock:{id} stock  初始化库存
        // TODO: 如果是秒杀券 → DEL seckill:ordered:{id}     清空已购集合
    }

    @Override
    public Voucher getById(Long id) {
        // TODO: 查询优惠券详情
        return null;
    }

    @Override
    public Long buyVoucher(Long voucherId, Long userId) {
        // TODO: 1. 判断库存（GET seckill:stock:{id} > 0）
        // TODO: 2. 判断限购（SISMEMBER seckill:ordered:{id}）
        // TODO: 3. 扣减库存（DECRBY seckill:stock:{id} 1）
        // TODO: 4. 生成订单
        return null;
    }

    @Override
    public Long seckillVoucher(Long voucherId, Long userId) {
        // TODO: Lua 脚本原子操作
        //   1. 判断库存 > 0
        //   2. 判断用户未购买过
        //   3. 扣减库存 + 加入已购集合
        // TODO: 成功后放入阻塞队列，异步创建订单
        return null;
    }

    @Override
    public int getSeckillStock(Long voucherId) {
        // TODO: GET seckill:stock:{voucherId}
        return 0;
    }
}
