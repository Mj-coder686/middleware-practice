package com.mj.middleware.redis.service.impl;

import cn.hutool.core.bean.BeanUtil;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.middleware.redis.entity.RedisData;
import com.mj.middleware.redis.entity.Voucher;
import com.mj.middleware.redis.mapper.VoucherMapper;
import com.mj.middleware.redis.service.IVoucherService;
import com.mj.middleware.redis.until.RedisWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 优惠券业务实现 — TODO: 使用 Redis String / Lua 脚本实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {
    /**
     * 优惠券基础信息缓存前缀（Hash 结构或 String 结构）
     * 示例：cache:voucher:info:101
     */
    public static final String CACHE_VOUCHER_KEY = "cache:voucher:info:";

    /**
     * 优惠券缓存的标准过期时间（30分钟）
     */
    public static final Long CACHE_VOUCHER_TTL = 30L;

    /**
     * 穿透拦截：防恶意刷券时，存入 Redis 空值的过期时间（2分钟）
     */
    public static final Long CACHE_NULL_TTL = 2L;


    // ====================== 2. 秒杀库存与资格（高并发核心压测） ======================

    /**
     * 秒杀优惠券的真实库存缓存前缀（String 结构，用于 Redis 预减库存）
     * 示例：seckill:voucher:stock:101
     */
    public static final String SECKILL_STOCK_KEY = "seckill:voucher:stock:";

    /**
     * 布隆过滤器名称（用于在最外层拦截不存在的优惠券ID，防穿透）
     */
    public static final String VOUCHER_BLOOM_FILTER = "bloom:vouchers";


    // ====================== 3. Redisson 分布式锁（核心大闸） ======================

    /**
     * 一人一单分布式锁前缀（锁住用户，防止同一个用户开多线程脚本同时刷单）
     * 示例：lock:order:user:12345
     */
    public static final String LOCK_USER_ORDER_KEY = "lock:order:user:";

    /**
     * 减库存分布式锁前缀（如果不用高并发 Lua 脚本，用这个锁住商品库存，防超卖）
     * 示例：lock:voucher:stock:101
     */
    public static final String LOCK_VOUCHER_STOCK_KEY = "lock:voucher:stock:";

    // 声明一个线程池，专门用来做缓存的异步重建，避免频繁创建线程
    private static final ExecutorService CACHE_REBUILD_EXECUTOR =
            Executors.newFixedThreadPool(10);

    private final VoucherMapper voucherMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void add(Voucher voucher) {
        // 1. 保存到 MySQL 数据库
        save(voucher);

        // 2. 🚨 核心：同步做缓存预热（逻辑过期配置）
        RedisData redisData = new RedisData();
        redisData.setData(voucher);

        // 假设这场秒杀活动持续 30 分钟，那逻辑过期时间就设为 30 分钟后
        redisData.setExpireTime(LocalDateTime.now().plusMinutes(30L));

        // 3. 写入 Redis，注意：不设置真正的 TTL，让它永不过期
        String cacheKey = CACHE_VOUCHER_KEY + voucher.getId();
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(redisData));
    }

    @Override
    public Voucher getById(Long id) {
        if (id == null) {
            return null;
        }
        // 1. 每一个优惠券都是独立的 Key（规范：前缀 + 优惠券ID）
        String key = CACHE_VOUCHER_KEY + id;

        // 2. 从 Redis 查出 JSON 字符串
        String voucherJson = stringRedisTemplate.opsForValue().get(key);

        // 3. 判断是否命中
        if (StrUtil.isNotBlank(voucherJson)) {
            // 命中真实数据，反序列化返回
            return JSONUtil.toBean(voucherJson, Voucher.class);
        }

        // 4. 🚨 核心防线：判断是否命中了“防止穿透的空值”
        // 如果 voucherJson 不为 null，但又是空白字符（那只能是我们之前塞进去的 ""）
        if (voucherJson != null) {
            return null; // 直接拦截返回，绝不走数据库！
        }

        // 5. 💥 缓存击穿兜底：开始尝试获取互斥锁
        String lockKey = "lock:voucher:" + id;
        Voucher voucher = null;
        try {
            // 尝试获取锁（SETNX），如果拿不到，说明有别人正在查数据库重构缓存
            boolean isLock = tryLock(lockKey);
            if (!isLock) {
                // 没拿到锁，稍微眯一会儿，然后重新调用本方法（Double Check 思路）
                Thread.sleep(50);
                return getById(id);
            }

            // 6. 拿到锁了，先做二次检查（Double Check），防止在等锁期间别人已经把缓存做好了
            String doubleCheckJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(doubleCheckJson)) {
                return JSONUtil.toBean(doubleCheckJson, Voucher.class);
            }

            // 7. 真正查数据库
            voucher = lambdaQuery().eq(Voucher::getId, id).one();

            // 8. 数据库有数据：写入缓存，带上随机扰动防雪崩
            if (voucher != null) {
                // 标准过期时间 30 分钟 + 随机 1~5 分钟扰动，错开过期时间
                long randomTtl = CACHE_VOUCHER_TTL + RandomUtil.randomInt(1, 5);
                stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(voucher), randomTtl, TimeUnit.MINUTES);
                return voucher;
            }

            // 9. 🚨 数据库没数据：缓存空对象，完美防穿透
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 10. 无论如何，最后必须释放锁
            unlock(lockKey);
        }

        return null;
    }

    public Voucher getByIdUnion(Long id) {
        if (id == null) return null;
        String cacheKey = CACHE_VOUCHER_KEY + id;

        // 1. 先查 Redis
        String json = stringRedisTemplate.opsForValue().get(cacheKey);

        // 2. 🚨 穿透防线第一步：判断是否命中了防穿透的“空字符串”
        // 如果 json 是 "" (不是 null，也不是空白字符串，就是个真正的空对象标记)
        if (json != null && json.equals("")) {
            return null; // 证明是骗子 Key，直接拦截（防穿透成功）
        }

        // 3. 如果查出来彻底是 null，说明什么？说明 Redis 里啥也没有！
        // 既然啥也没有，说明它既没有被“预热”，也不是“骗子 Key”，它大概率是一张普通的非秒杀券
        if (StrUtil.isBlank(json)) {
            // 走普通加锁查库逻辑（或者传统的互斥锁方案，查不到就塞空字符串防穿透）
            return getById(id);
        }

        // 4. 💥 走到这里，说明 json 既不是空字符串，也不是 null，而是有货！
        // 既然有货，那它一定是一个打包了“逻辑过期时间”的秒杀券数据
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        Voucher voucher = JSONUtil.toBean((JSONObject) redisData.getData(), Voucher.class);
        LocalDateTime expireTime = redisData.getExpireTime();

        // 5. ⏱️ 击穿防线：判断逻辑过期时间
        if (expireTime.isAfter(LocalDateTime.now())) {
            return voucher; // 没过期，直接返回
        }

        // 6. 过期了，抢锁异步重建
        String lockKey = "lock:voucher:mutex:" + id;
        if (tryLock(lockKey)) {
            String doubleCheckJson = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isNotBlank(doubleCheckJson)) {
                return JSONUtil.toBean(doubleCheckJson, Voucher.class);
            }
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    this.saveVoucherToRedis(id, 30L); // 异步刷新
                } finally {
                    unlock(lockKey);
                }
            });
        }

        // 7. 逻辑过期特征：没拿到锁也直接返回旧数据
        return voucher;
    }

    private void saveVoucherToRedis(Long id, long l) {
        Voucher voucher = lambdaQuery().eq(Voucher::getId, id).one();
        RedisData redisData = new RedisData();
        redisData.setData(voucher);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(l));
        stringRedisTemplate.opsForValue().set(CACHE_VOUCHER_KEY + id, JSONUtil.toJsonStr(redisData));
        log.debug("缓存重建成功：{}", id);
    }


    /**
     * 核心辅助方法：利用 SETNX 抢锁
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }

    /**
     * 核心辅助方法：释放锁
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public Long buyVoucher(Long voucherId, Long userId) {
        // TODO: 1. 判断库存（GET seckill:stock:{id} > 0）
        // TODO: 2. 判断限购（SISMEMBER seckill:ordered:{id}）
        // TODO: 3. 扣减库存（DECRBY seckill:stock:{id} 1）
        // TODO: 4. 生成订单
        return null;
    }

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }
    private final RabbitTemplate rabbitTemplate;
    @Override
    public Long seckillVoucher(Long voucherId, Long userId) {
//        生成唯一序列
        RedisWorker redisWorker = new RedisWorker();
        long orderId = redisWorker.nextId("order");

        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString()
        );
        int r = result.intValue();
        if (r == 1){
            throw new RuntimeException("库存不足");
        }
        if (r == 2){
            throw new RuntimeException("不能重复下单");
        }
//        MQ来转发
        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("orderId", orderId);
        messageMap.put("voucherId", voucherId);
        messageMap.put("userId", userId);
        rabbitTemplate.convertAndSend("seckill.exchange", "seckill.order", messageMap);
        return orderId;
    }

    @Override
    public int getSeckillStock(Long voucherId) {
        // TODO: GET seckill:stock:{voucherId}
        return 0;
    }
}
