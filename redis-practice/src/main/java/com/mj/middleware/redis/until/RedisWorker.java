package com.mj.middleware.redis.until;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class RedisWorker {
    @Resource
    private StringRedisTemplate  stringRedisTemplate;

    /**
     * 时间戳起始值，用于生成唯一ID的时间部分
     * 这个常量定义了时间戳计算的基准时间点
     */
    private static final long BEGIN_TIMESTAMP = 1796083200L;

    /**
     * 计数器位数，用于控制序列号部分的位数长度
     * 该常量决定了在同一毫秒内可生成的ID数量上限
     */
    private static final long COUNT_BITS = 32;

    public long nextId(String keyPrefix) {
//        生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long longtemp = nowSecond - BEGIN_TIMESTAMP;
//        生成序列号
        String yyyyMMdd = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + yyyyMMdd);
//        拼接并返回
        return longtemp << COUNT_BITS | count;
    }
}
