package com.mj.middleware.redis.Linstener;


import com.mj.middleware.redis.service.IVoucherService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class SeckillListener {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private IVoucherService voucherService;

    @RabbitListener(queues = "seckill.order.queue")
    public void listenSeckillOrder(Map<String,Object> messageMap) throws InterruptedException {
        Long voucherId = (Long) messageMap.get("voucherId");
        Long userId = (Long) messageMap.get("orderId");
        Long orderId = (Long) messageMap.get("userId");

        RLock lock = redissonClient.getLock("lock:order:user" + userId);

        boolean isLock = lock.tryLock(5, 10, TimeUnit.SECONDS);
        if (!isLock) {
            return;
        }
        try {
            voucherService.seckillVoucher(voucherId, userId);
        } finally {
            lock.unlock();
        }
    }

}
