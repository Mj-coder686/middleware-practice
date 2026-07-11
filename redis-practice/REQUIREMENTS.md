# Redis 练习模块 — 需求文档

> 本文档对应 `redis-practice` 模块中已搭建好的 Controller / Service / Mapper 骨架。
> 每个需求标注了涉及的 Redis 数据结构，填 TODO 时按需求逐条实现即可。

---

## 一、用户签到

**涉及数据结构：** Bitmap、HyperLogLog

**实体：** `SignRecord`

**Redis Key 设计：**

| Key | 类型 | 说明 |
|-----|------|------|
| `sign:{userId}:{yyyyMM}` | Bitmap | 每天 1 bit，offset = 日期-1 |
| `sign:count:{yyyyMM}` | HyperLogLog | 全站签到人数去重统计 |

### 需求清单

- [ ] **S1-1 用户签到** — `SignService.sign(userId)`
  - 使用 `SETBIT sign:{userId}:{yyyyMM} dayOffset 1` 记录当天签到
  - 需要先算出当天是本月第几天（dayOffset = dayOfMonth - 1）

- [ ] **S1-2 查询本月签到记录** — `SignService.getSignRecord(userId, month)`
  - 使用 `BITFIELD sign:{userId}:{month} GET u8 0` 读取前 8 天状态
  - 或逐日 `GETBIT`，返回 `SignRecord` 对象

- [ ] **S1-3 连续签到天数** — `SignService.getConsecutiveDays(userId)`
  - 从今天开始向前遍历 Bitmap，统计连续为 1 的天数
  - 遇到 0 或超出月初则停止

- [ ] **S1-4 领取连续签到奖励** — `SignService.claimReward(userId)`
  - 判断连续签到天数 ≥ 7 / 14 / 28
  - 每个档位只可领取一次（用一个 Set 记录已领取的档位）
  - 领取后写入 `SignRecord.rewardClaimed = true`

- [ ] **S1-5 签到总人数（HyperLogLog）** — `SignService.countUniqueSignUsers(month)`
  - 签到时同时 `PFADD sign:count:{month} userId`
  - 查询时 `PFCOUNT sign:count:{month}`，误差率 ~0.81%

---

## 二、大V发博客 & 点赞

**涉及数据结构：** Hash、Set、SortedSet、List、String（自增）

**实体：** `Blog`

**Redis Key 设计：**

| Key | 类型 | 说明 |
|-----|------|------|
| `global:blogId` | String | 博客 ID 自增器，INCR 生成唯一 ID |
| `blog:{blogId}` | Hash | 博客详情（title, content, userId, likedCount, createTime...） |
| `blog:liked:{blogId}` | Set | 点赞的用户 ID 集合 |
| `blog:of:{userId}` | List | 某博主的所有博客 ID，按时间倒序 |

### 需求清单

- [ ] **S2-1 发布博客** — `BlogService.publish(blog)`
  1. `INCR global:blogId` 生成 ID
  2. `HSET blog:{id}` 存储各字段
  3. `LPUSH blog:of:{userId}` 插入博客 ID
  4. 调用 `feedService.pushToFollowers()` 推送到粉丝 Feed 流

- [ ] **S2-2 查询博客详情** — `BlogService.getById(id)`
  - `HGETALL blog:{id}` 取出全部字段
  - 判断当前用户是否已点赞（`SISMEMBER blog:liked:{id} userId`）

- [ ] **S2-3 点赞** — `BlogService.like(blogId, userId)`
  1. `SISMEMBER blog:liked:{blogId} userId` 判断是否已赞
  2. 未赞 → `SADD blog:liked:{blogId} userId` + `HINCRBY blog:{id} likedCount 1`
  3. 返回 `true` 表示点赞成功，`false` 表示重复点赞

- [ ] **S2-4 取消点赞** — `BlogService.unlike(blogId, userId)`
  1. `SISMEMBER` 判断是否已赞
  2. 已赞 → `SREM blog:liked:{blogId} userId` + `HINCRBY blog:{id} likedCount -1`
  3. 返回 `true` 取消成功

- [ ] **S2-5 查询点赞数** — `BlogService.getLikeCount(blogId)`
  - `HGET blog:{id} likedCount` 或 `SCARD blog:liked:{id}`

- [ ] **S2-6 查询点赞用户列表** — `BlogService.getLikedUsers(blogId, count)`
  - `SRANDMEMBER blog:liked:{blogId} count` 随机返回 N 个点赞用户

- [ ] **S2-7 查询博主博客列表** — `BlogService.getBlogsByUser(userId, pageNum, pageSize)`
  - `LRANGE blog:of:{userId} (pageNum-1)*pageSize pageNum*pageSize-1`
  - 拿到博客 ID 列表后逐个 `HGETALL`

---

## 三、Feed 推拉流 & 关注关系

**涉及数据结构：** Set、List

**实体：** `Feed`

**Redis Key 设计：**

| Key | 类型 | 说明 |
|-----|------|------|
| `follow:{userId}` | Set | 我关注了谁 |
| `fans:{userId}` | Set | 我的粉丝是谁 |
| `feed:{userId}` | List | 我的 Feed 流（推模型，LPUSH + LTRIM） |

### 需求清单

- [ ] **S3-1 关注** — `FeedService.follow(followerId, followeeId)`
  1. `SADD follow:{followerId} followeeId`
  2. `SADD fans:{followeeId} followerId`

- [ ] **S3-2 取消关注** — `FeedService.unfollow(followerId, followeeId)`
  1. `SREM follow:{followerId} followeeId`
  2. `SREM fans:{followeeId} followerId`

- [ ] **S3-3 查询关注列表** — `FeedService.getFollowing(userId)`
  - `SMEMBERS follow:{userId}`

- [ ] **S3-4 查询粉丝列表** — `FeedService.getFollowers(userId)`
  - `SMEMBERS fans:{userId}`

- [ ] **S3-5 查询共同关注** — `FeedService.getCommonFollowing(userId1, userId2)`
  - `SINTER follow:{userId1} follow:{userId2}`

- [ ] **S3-6 推送到粉丝 Feed** — `FeedService.pushToFollowers(authorId, blogId/productId)`
  - `SMEMBERS fans:{authorId}` 获取全部粉丝
  - 遍历粉丝，`LPUSH feed:{粉丝id} feedData`
  - `LTRIM feed:{粉丝id} 0 999` 只保留最近 1000 条
  - **优化：** 粉丝数 > 阈值（如 5000）的大V不推送，走拉模型

- [ ] **S3-7 获取我的 Feed 流** — `FeedService.getFeed(userId, lastFeedId, pageSize)`
  - **推模型：** `LRANGE feed:{userId} start end` 直接从自己的 Feed 列表取
  - **拉模型：** 遍历 `SMEMBERS follow:{userId}`，拉取每个关注人的最新博客/商品，合并排序
  - 支持滚动分页：传入 `lastFeedId`，从该位置继续取下一页

---

## 四、商品发布 & 商品 Feed 流

**涉及数据结构：** Hash、ZSet、Set

**实体：** `Product`

**Redis Key 设计：**

| Key | 类型 | 说明 |
|-----|------|------|
| `global:productId` | String | 商品 ID 自增器 |
| `product:{productId}` | Hash | 商品详情 |
| `product:of:{merchantId}` | ZSet | 商家的商品列表（score = 发布时间戳） |
| `product:category:{category}` | ZSet | 分类商品列表（score = 销量） |
| `product:liked:{productId}` | Set | 收藏商品的用户集合 |

### 需求清单

- [ ] **S4-1 发布商品** — `ProductService.publish(product)`
  1. `INCR global:productId` 生成 ID
  2. `HSET product:{id}` 存储商品详情
  3. `ZADD product:of:{merchantId} timestamp productId` 加入商家列表
  4. `ZADD product:category:{category} 0 productId` 加入分类列表
  5. 调用 `feedService.pushToFollowers(merchantId, productId)` 推送到关注者 Feed

- [ ] **S4-2 查询商品详情** — `ProductService.getById(id)`
  - `HGETALL product:{id}`
  - 判断当前用户是否已收藏

- [ ] **S4-3 上架 / 下架** — `ProductService.putOnSale / pullOffSale`
  - `HSET product:{id} status 1` 或 `status 0`

- [ ] **S4-4 收藏 / 取消收藏** — `ProductService.like / unlike`
  - 和博客点赞同理：`SADD/SREM` + `HINCRBY likedCount`

- [ ] **S4-5 按商家查询商品列表** — `ProductService.getByMerchant(merchantId, pageNum, pageSize)`
  - `ZREVRANGE product:of:{merchantId} start end`（按时间倒序）
  - 拿到 ID 列表后逐个 `HGETALL`

- [ ] **S4-6 按分类查询商品（销量排序）** — `ProductService.getByCategory(category, pageNum, pageSize)`
  - `ZREVRANGE product:category:{category} start end`（按销量倒序）

---

## 五、购买优惠券（普通模式）

**涉及数据结构：** String、Set

**实体：** `Voucher`、`VoucherOrder`

**Redis Key 设计：**

| Key | 类型 | 说明 |
|-----|------|------|
| `seckill:stock:{voucherId}` | String | 优惠券库存（数值） |
| `seckill:ordered:{voucherId}` | Set | 已下单的用户 ID 集合 |

### 需求清单

- [ ] **S5-1 新增优惠券** — `VoucherService.add(voucher)`
  - 存储优惠券信息
  - 初始化 Redis 库存：`SET seckill:stock:{id} stock`

- [ ] **S5-2 购买普通优惠券** — `VoucherService.buyVoucher(voucherId, userId)`
  1. `GET seckill:stock:{voucherId}` 判断库存 > 0
  2. `SISMEMBER seckill:ordered:{voucherId} userId` 判断未购买过
  3. `DECRBY seckill:stock:{voucherId} 1` 扣减库存
  4. `SADD seckill:ordered:{voucherId} userId` 记录已购
  5. 生成 `VoucherOrder`，返回订单 ID

---

## 六、秒杀抢券（Lua 脚本原子化）

**涉及数据结构：** 同上，核心是 Lua 脚本保证原子性

**问题背景：** 普通模式的「判断 + 扣减」不是原子操作，并发下会超卖。

### 需求清单

- [ ] **S6-1 秒杀抢券（Lua 脚本）** — `VoucherService.seckillVoucher(voucherId, userId)`
  - 编写 Lua 脚本，一个脚本内完成：
    ```lua
    -- KEYS[1] = seckill:stock:{voucherId}
    -- KEYS[2] = seckill:ordered:{voucherId}
    -- ARGV[1] = userId
    -- 1. 判断库存
    if tonumber(redis.call('get', KEYS[1])) <= 0 then
      return 1  -- 库存不足
    end
    -- 2. 判断是否已购买
    if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
      return 2  -- 已购买
    end
    -- 3. 扣减库存 + 记录用户
    redis.call('decrby', KEYS[1], 1)
    redis.call('sadd', KEYS[2], ARGV[1])
    return 0  -- 成功
    ```
  - Lua 执行成功后，将订单放入**阻塞队列**，异步线程消费创建订单

- [ ] **S6-2 查询秒杀库存** — `VoucherService.getSeckillStock(voucherId)`
  - `GET seckill:stock:{voucherId}`

---

## Redis 数据结构速查

| 场景 | 数据结构 | 核心命令 |
|------|----------|----------|
| 签到 | Bitmap | `SETBIT` `GETBIT` `BITFIELD` |
| 签到人数统计 | HyperLogLog | `PFADD` `PFCOUNT` |
| 博客详情 | Hash | `HSET` `HGET` `HGETALL` `HINCRBY` |
| 点赞用户集合 | Set | `SADD` `SREM` `SISMEMBER` `SCARD` `SRANDMEMBER` |
| 关注/粉丝 | Set | `SADD` `SREM` `SMEMBERS` `SINTER` |
| 博主博客列表 | List | `LPUSH` `LRANGE` `LTRIM` |
| Feed 流 | List | `LPUSH` `LTRIM` `LRANGE` |
| 商家/分类商品 | SortedSet | `ZADD` `ZREVRANGE` |
| 秒杀库存 | String | `SET` `GET` `DECRBY` |
| 原子秒杀 | Lua | `redisTemplate.execute(script)` |

---

## 实现顺序建议

```
① 签到（最简单，熟悉 Redis 基本操作）
② 博客 & 点赞（Hash + Set 组合）
③ 关注关系 + Feed 推流（Set + List）
④ 商品 + Feed 推送（复用③的 Feed 机制）
⑤ 购买优惠券（String 扣库存）
⑥ 秒杀 Lua 脚本（最终 boss，原子操作 + 阻塞队列）
```
