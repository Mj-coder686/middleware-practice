# Redis 学习指南

## 一、概述

**Redis** 是开源的高性能 Key-Value 内存数据库，支持多种数据结构，广泛用于缓存、消息队列、分布式锁、排行榜等场景。

**核心特性：**
- 基于内存，读写速度极快（10万+ QPS）
- 支持丰富的数据结构（String、Hash、List、Set、ZSet、Stream、Bitmap、HyperLogLog）
- 支持持久化（RDB / AOF）
- 支持主从复制、哨兵、集群

## 二、环境搭建

### Docker 启动

```bash
# 单机
docker run -d --name redis -p 6379:6379 redis:7

# 带密码
docker run -d --name redis -p 6379:6379 redis:7 redis-server --requirepass 123456

# Redis Stack（含 RedisInsight GUI）
docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```

### 依赖配置

```xml
<!-- Spring Data Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Redisson（分布式锁） -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.2</version>
</dependency>
```

### application.yml

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2
```

## 三、核心知识点

### 🟢 必须掌握

#### 1. 五大数据类型

| 类型 | 底层结构 | 典型应用 |
|------|----------|----------|
| **String** | SDS | 缓存、计数器、分布式锁、Session |
| **Hash** | ziplist / hashtable | 对象存储、购物车、配置 |
| **List** | quicklist | 消息队列、最新列表、时间线 |
| **Set** | intset / hashtable | 去重、共同好友、抽奖 |
| **ZSet** | ziplist / skiplist+hashtable | 排行榜、延迟队列、限流 |

**对应代码：** `string/RedisStringService.java`, `hash/RedisHashService.java`, `list/RedisListService.java`, `set/RedisSetService.java`, `zset/RedisZSetService.java`

#### 2. Spring Data Redis API

```java
// 注入
@Autowired
private StringRedisTemplate stringRedisTemplate;  // Key-Value 都是 String
@Autowired
private RedisTemplate<String, Object> redisTemplate;  // 支持 Object（需配置序列化）

// String 操作
stringRedisTemplate.opsForValue().set("key", "value");
stringRedisTemplate.opsForValue().get("key");
stringRedisTemplate.opsForValue().increment("counter");  // 原子自增

// Hash 操作
stringRedisTemplate.opsForHash().put("user:1", "name", "张三");
stringRedisTemplate.opsForHash().entries("user:1");

// List 操作
stringRedisTemplate.opsForList().rightPush("queue", "msg");
stringRedisTemplate.opsForList().leftPop("queue");

// Set 操作
stringRedisTemplate.opsForSet().add("tags", "java", "redis");
stringRedisTemplate.opsForSet().intersect("tags:user1", "tags:user2");

// ZSet 操作
stringRedisTemplate.opsForZSet().add("rank", "user1", 100);
stringRedisTemplate.opsForZSet().reverseRange("rank", 0, 9);  // Top 10
```

#### 3. 序列化配置

```java
// 推荐：Key 用 String，Value 用 JSON
template.setKeySerializer(new StringRedisSerializer());
template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
```

**对应代码：** `config/RedisConfig.java`

---

### 🟡 进阶掌握

#### 4. Redisson 分布式锁

```java
RLock lock = redissonClient.getLock("myLock");
try {
    boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS); // 等待3s，持锁10s
    if (locked) {
        // 业务逻辑
    }
} finally {
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

**Redisson 锁类型：**
- `getLock()` — 可重入锁（默认）
- `getFairLock()` — 公平锁
- `getReadWriteLock()` — 读写锁
- `getMultiLock()` — 联锁（同时锁多个资源）

**对应代码：** `lock/RedisLockService.java`

#### 5. 缓存三大问题

| 问题 | 现象 | 解决方案 |
|------|------|----------|
| **穿透** | 查询不存在的数据 | 布隆过滤器、缓存空值 |
| **击穿** | 热点 key 过期瞬间大量请求 | 互斥锁、逻辑过期 |
| **雪崩** | 大量 key 同时过期 | 随机 TTL、集群高可用 |

**对应代码：** `cache/CacheStrategyService.java`

#### 6. 购物车实现

```
Hash 结构：cart:{userId} → {productId: quantity, ...}
HSET cart:1001 prod_001 2     — 添加/修改
HDEL cart:1001 prod_001        — 删除
HGETALL cart:1001              — 查看购物车
HLEN cart:1001                 — 商品种类数
```

**对应代码：** `hash/RedisHashService.java` (addToCart, getCart, updateCartItem)

---

### 🔴 高级/面试常问

#### 7. Redis 为什么快？

1. **纯内存操作** — 数据存在内存中
2. **单线程模型** — 避免上下文切换和锁竞争（Redis 6.0+ IO 线程多线程，命令执行仍单线程）
3. **IO 多路复用** — epoll 实现高并发连接处理
4. **高效数据结构** — SDS、ziplist、skiplist 等

#### 8. 持久化方式

| 方式 | 原理 | 优点 | 缺点 |
|------|------|------|------|
| **RDB** | 定时快照 | 恢复快、文件小 | 可能丢最后一次快照后的数据 |
| **AOF** | 追加写命令日志 | 数据安全（everysec 最多丢1s） | 文件大、恢复慢 |
| **混合** | RDB + 增量 AOF | 兼顾两者优点 | Redis 4.0+ |

#### 9. 淘汰策略

8 种淘汰策略（配置 `maxmemory-policy`）：
- `noeviction` — 不淘汰，内存满时写入报错（默认）
- `allkeys-lru` — 所有 key 中淘汰最近最少使用的（**推荐**）
- `volatile-lru` — 有 TTL 的 key 中淘汰 LRU
- `allkeys-lfu` — 所有 key 中淘汰最不常使用的
- `volatile-ttl` — 淘汰 TTL 最短的 key
- `allkeys-random` / `volatile-random` — 随机淘汰

#### 10. 常见面试题

**Q: 分布式锁如何防止误删？**
A: 加锁时设置唯一标识（如 UUID），释放锁时先检查是否是自己持有的锁（Lua 脚本保证原子性）。

**Q: Redis 和 Memcached 的区别？**
A: Redis 支持多种数据结构、持久化、发布订阅、Lua 脚本；Memcached 只支持 String，无持久化，多线程。

**Q: 缓存和数据库双写一致性？**
A: 推荐 Cache Aside 模式：先更新 DB → 再删除缓存（而不是更新缓存）。延迟双删进一步保证一致性。

**Q: Redis 集群方案？**
A: 主从复制（读写分离）→ 哨兵（自动故障转移）→ Cluster（数据分片，16384 个 slot）。

## 四、最佳实践

1. **Key 命名规范** — `业务:对象:id:属性`（如 `user:1001:info`）
2. **必须设置 TTL** — 防止内存泄漏
3. **避免大 Key** — String < 10KB，Hash/Set < 5000 元素
4. **批量操作用 pipeline** — 减少网络 RTT
5. **使用 Lua 脚本保证原子性** — 如分布式锁释放
6. **生产环境用 Redisson 而非手动 SETNX**

## 五、参考链接

- [Redis 官方文档](https://redis.io/docs/)
- [Redis 命令参考](https://redis.io/commands/)
- [Redisson GitHub](https://github.com/redisson/redisson)
- [Spring Data Redis](https://docs.spring.io/spring-data/redis/reference/)
