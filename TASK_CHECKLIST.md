# 中间件练习任务清单

> 每个模块的骨架（POM + application.yml + 实体类 + 启动类 + 基础配置）已经帮你搭好
> 你需要按下面的清单，**一个一个写对应的类**，遇到不会的可以问我

---

## 模块零-a：mybatis-plus-practice（端口 8081）

### 前置条件
- 无需安装数据库，使用 H2 内存数据库，启动即可运行
- 表结构和测试数据已在 `db/schema.sql` + `db/data.sql` 准备好

### 已有的骨架文件（不用动）
```
✅ MyBatisPlusApplication.java  — 启动类
✅ config/MyBatisPlusConfig.java — 分页插件 + 乐观锁插件 + 防全表更新 + 自动填充
✅ config/Knife4jConfig.java     — OpenAPI 配置
✅ entity/UserMP.java            — 用户实体 (@TableName + @TableLogic + @Version + 自动填充)
✅ entity/ProductMP.java         — 商品实体
✅ mapper/UserMapper.java        — 继承 BaseMapper<UserMP>
✅ mapper/ProductMapper.java     — 继承 BaseMapper<ProductMP>
✅ application.yml               — H2 数据源 + MP 配置 + Knife4j
✅ db/schema.sql + data.sql      — 建表 + 测试数据
```

### 任务清单

| 序号 | 要写的类 | 练习要点 |
|------|---------|---------|
| 1 | `service/IUserService.java` | 继承 `IService<UserMP>`，定义 3 个自定义方法 |
| 2 | `service/impl/UserServiceImpl.java` | **核心** — LambdaQueryWrapper + 分页 + 批量插入 |
| 3 | `service/IProductService.java` | 继承 `IService<ProductMP>`，定义 5 个自定义方法 |
| 4 | `service/impl/ProductServiceImpl.java` | **核心** — QueryWrapper + 聚合 + 乐观锁 + UpdateWrapper |
| 5 | `controller/UserController.java` | REST 接口：CRUD + 批量 + 条件查询 + 分页 |
| 6 | `controller/ProductController.java` | REST 接口：CRUD + 条件查询 + 聚合 + 乐观锁 |

### IUserService 方法签名

```java
public interface IUserService extends IService<UserMP> {
    /** 根据用户名查询 — 练习 LambdaQueryWrapper.eq */
    UserMP getByUsername(String username);

    /** 条件分页查询 — 练习 like + eq + 分页 */
    IPage<UserMP> pageQuery(String keyword, Integer status, int pageNum, int pageSize);

    /** 批量插入 — 练习 saveBatch */
    boolean batchInsertDemo();
}
```

### IProductService 方法签名

```java
public interface IProductService extends IService<ProductMP> {
    /** 按分类和价格范围查询 — 练习 LambdaQueryWrapper 链式条件 */
    List<ProductMP> queryByCondition(String category, BigDecimal minPrice, BigDecimal maxPrice);

    /** 按品牌统计数量 — 练习 QueryWrapper + select + groupBy */
    List<Map<String, Object>> countByBrand();

    /** 多条件复杂查询 — 练习嵌套 AND/OR 条件 */
    List<ProductMP> complexQuery(String keyword, String brand, BigDecimal minPrice, Integer minStock);

    /** 乐观锁更新 — 练习 @Version 机制 */
    boolean updateWithOptimisticLock(Long id, BigDecimal newPrice);

    /** 条件批量更新 — 练习 LambdaUpdateWrapper + setSql */
    boolean batchUpdatePrice(String category, BigDecimal increaseAmount);
}
```

### Controller 端点规划

```
UserController:
  POST   /user              — 新增
  GET    /user/{id}         — 根据 ID 查
  GET    /user/list         — 查全部
  PUT    /user              — 更新
  DELETE /user/{id}         — 删除（逻辑删除）
  DELETE /user/batch        — 批量删除
  POST   /user/batch-insert — 批量插入演示
  GET    /user/by-username  — 按用户名查
  GET    /user/page         — 分页查询

ProductController:
  POST   /product              — 新增
  GET    /product/{id}         — 根据 ID 查
  GET    /product/list         — 查全部
  GET    /product/by-condition  — 分类+价格范围
  GET    /product/complex       — 多条件复杂查询
  GET    /product/count-by-brand — 按品牌统计
  PUT    /product/optimistic-lock — 乐观锁更新
  PUT    /product/batch-price    — 批量涨价
```

---

## 模块零-b：redis-practice（端口 8082）

### 前置条件
- 本地启动 Redis（默认 localhost:6379）
- `docker run -p 6379:6379 redis:7`

### 已有的骨架文件（不用动）
```
✅ RedisApplication.java       — 启动类
✅ config/RedisConfig.java      — 自定义 RedisTemplate (String Key + JSON Value)
✅ config/Knife4jConfig.java    — OpenAPI 配置
✅ application.yml              — Redis 连接 + Lettuce 连接池 + Redisson 配置
```

### 任务清单

| 序号 | 要写的类 | 练习要点 |
|------|---------|---------|
| 1 | `string/RedisStringService.java` | **String 操作** — set/get/incr/SETNX/mset |
| 2 | `hash/RedisHashService.java` | **Hash 操作** + 购物车实战 + 对象存储 |
| 3 | `list/RedisListService.java` | **List 操作** + 简易消息队列 + 最新列表 |
| 4 | `set/RedisSetService.java` | **Set 操作** + 共同好友 + 抽奖 |
| 5 | `zset/RedisZSetService.java` | **ZSet 操作** + 排行榜 + 延迟队列 |
| 6 | `lock/RedisLockService.java` | **Redisson 分布式锁** — 可重入锁 + 公平锁 + 读写锁 |
| 7 | `cache/CacheStrategyService.java` | **缓存策略** — Cache Aside + 防穿透 + 防击穿 + 防雪崩 |
| 8 | `controller/RedisDemoController.java` | REST 接口聚合所有功能 |

### RedisStringService 方法清单

```java
// 基础操作
void set(String key, String value);                              // SET
void setWithExpire(String key, String value, long seconds);      // SETEX
String get(String key);                                          // GET
Boolean delete(String key);                                      // DEL
Boolean exists(String key);                                      // EXISTS
Boolean expire(String key, long seconds);                        // EXPIRE
Long getExpire(String key);                                      // TTL

// 计数器
Long increment(String key);                                      // INCR
Long incrementBy(String key, long delta);                        // INCRBY
Long decrement(String key);                                      // DECR

// 分布式锁（简易版）
Boolean setIfAbsent(String key, String value);                   // SETNX
Boolean setIfAbsentWithExpire(String key, String value, long s); // SET NX EX

// 批量
void multiSet(Map<String, String> map);                          // MSET
List<String> multiGet(Collection<String> keys);                  // MGET
```

### RedisHashService 方法清单

```java
// 基础操作
void hSet(String key, String field, String value);               // HSET
Object hGet(String key, String field);                           // HGET
void hMultiSet(String key, Map<String, String> map);             // HMSET
Map<Object, Object> hGetAll(String key);                         // HGETALL
Long hDelete(String key, String... fields);                      // HDEL
Boolean hExists(String key, String field);                       // HEXISTS
Set<Object> hKeys(String key);                                   // HKEYS
List<Object> hVals(String key);                                  // HVALS
Long hSize(String key);                                          // HLEN
Long hIncrement(String key, String field, long delta);           // HINCRBY

// 购物车实战
void addToCart(String userId, String productId, int quantity);   // key=cart:{userId}
void updateCartItem(String userId, String productId, int qty);
Map<Object, Object> getCart(String userId);
void removeFromCart(String userId, String productId);
Long getCartSize(String userId);

// 对象存储实战
void saveUser(String userId, Map<String, String> userInfo);     // key=user:{id} + 设置 TTL
String getUserField(String userId, String field);
```

### RedisListService 方法清单

```java
// 基础操作
Long leftPush(String key, String value);                         // LPUSH
Long rightPush(String key, String value);                        // RPUSH
String leftPop(String key);                                      // LPOP
String rightPop(String key);                                     // RPOP
List<String> range(String key, long start, long end);            // LRANGE
Long size(String key);                                           // LLEN
String index(String key, long index);                            // LINDEX

// 阻塞操作
List<String> blockingLeftPop(String key, long timeout);          // BLPOP
List<String> blockingRightPop(String key, long timeout);         // BRPOP

// 简易消息队列
void sendMessage(String queue, String message);                  // RPUSH 生产
String receiveMessage(String queue, long timeout);               // BLPOP 消费

// 最新列表（保留最近 N 条）
void addToList(String key, String value, long maxCount);         // LPUSH + LTRIM
```

### RedisSetService 方法清单

```java
// 基础操作
Long add(String key, String... values);                          // SADD
Set<String> members(String key);                                 // SMEMBERS
Boolean isMember(String key, String value);                      // SISMEMBER
Long size(String key);                                           // SCARD
Long remove(String key, String... values);                       // SREM
Set<String> randomMembers(String key, long count);               // SRANDMEMBER
Set<String> pop(String key, long count);                         // SPOP

// 集合运算
Set<String> intersect(String key1, String key2);                 // SINTER 交集
Set<String> union(String key1, String key2);                     // SUNION 并集
Set<String> difference(String key1, String key2);                // SDIFF 差集

// 共同好友
void follow(String userId, String targetId);
Set<String> commonFollows(String userId1, String userId2);

// 抽奖
void joinLottery(String lotteryId, String userId);
Long getLotteryCount(String lotteryId);
Set<String> drawWinners(String lotteryId, int count);            // SPOP 不重复中奖
```

### RedisZSetService 方法清单

```java
// 基础操作
Boolean add(String key, String value, double score);             // ZADD
Double score(String key, String value);                          // ZSCORE
Double incrementScore(String key, String value, double delta);   // ZINCRBY
Long size(String key);                                           // ZCARD
Long remove(String key, String... values);                       // ZREM

// 排名
Long rank(String key, String value);                             // ZRANK 升序
Long reverseRank(String key, String value);                      // ZREVRANK 降序
Set<String> topN(String key, long count);                        // ZREVRANGE Top N
Set<TypedTuple<String>> topNWithScores(String key, long count);  // 带分数

// 范围查询
Set<String> rangeByScore(String key, double min, double max);    // ZRANGEBYSCORE
Long count(String key, double min, double max);                  // ZCOUNT

// 排行榜实战
void updateScore(String leaderboard, String userId, double score);
void increaseScore(String leaderboard, String userId, double delta);
Set<String> getTopN(String leaderboard, int n);
Long getUserRank(String leaderboard, String userId);

// 延迟队列实战（score=时间戳，ZRANGEBYSCORE 取到期消息）
void addDelayedMessage(String queue, String message, long timestamp);
Set<String> getExpiredMessages(String queue, long currentTimestamp);
```

### RedisLockService 方法清单

```java
// Redisson 可重入锁
boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Runnable task);
boolean executeWithLock(String lockKey, Runnable task);   // 简化版，3s等待/10s持有

// 公平锁 — 按请求顺序获取
boolean tryFairLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Runnable task);

// 读写锁 — 读共享、写互斥
<T> T executeWithReadLock(String key, Supplier<T> reader);
void executeWithWriteLock(String key, Runnable writer);
```

### CacheStrategyService 方法清单

```java
// Cache Aside — 先查缓存 → miss 查 DB → 回填
<T> T cacheAside(String key, Class<T> type, Supplier<T> dbLoader, long ttl);

// 防穿透：缓存空值
void cacheNullValue(String key);

// 防击穿：互斥锁 + 双重检查
<T> T withMutexLock(String key, String lockKey, Class<T> type, Supplier<T> dbLoader, long ttl);

// 防雪崩：随机 TTL
void setWithRandomTTL(String key, String value, long baseTTL, long randomRange);
```

---

## 模块零-c：rabbitmq-practice（端口 8083）

### 前置条件
- 本地启动 RabbitMQ（默认 localhost:5672）
- `docker run -p 5672:5672 -p 15672:15672 rabbitmq:3-management`
- 管理后台 http://localhost:15672 (guest/guest)

### 已有的骨架文件（不用动）
```
✅ RabbitMQApplication.java      — 启动类
✅ config/RabbitMQConstants.java  — 所有交换机/队列/路由键常量定义
✅ config/RabbitMQConfig.java     — 所有 Exchange/Queue/Binding Bean 声明 + RabbitTemplate 回调
✅ application.yml                — RabbitMQ 连接 + publisher-confirm + prefetch + 手动 ACK + 重试
```

### 任务清单

| 序号 | 要写的类 | 练习要点 |
|------|---------|---------|
| 1 | `simple/SimpleDemo.java` | **简单队列** — 最基础的生产消费模型 |
| 2 | `workqueue/WorkQueueDemo.java` | **工作队列** — 多消费者竞争消费（轮询 vs 公平） |
| 3 | `direct/DirectDemo.java` | **Direct 交换机** — 精确匹配 routing key |
| 4 | `fanout/FanoutDemo.java` | **Fanout 交换机** — 广播模式 |
| 5 | `topic/TopicDemo.java` | **Topic 交换机** — 通配符匹配（* 和 #） |
| 6 | `ttl/TtlDemo.java` | **TTL + 死信队列** — 消息过期处理 |
| 7 | `delay/DelayDemo.java` | **延迟队列** — 订单超时取消场景 |
| 8 | `confirm/ConfirmDemo.java` | **发布确认** — 保证消息可靠到达 Broker |
| 9 | `priority/PriorityDemo.java` | **优先级队列** — VIP 优先处理 |

### 各 Demo 写法说明

每个 Demo 都是一个 **@RestController**，同时包含**生产者接口**和**消费者监听**。

```java
// === 1. SimpleDemo ===
// 路径: /simple/send
// 生产: rabbitTemplate.convertAndSend(SIMPLE_QUEUE, message);
// 消费: @RabbitListener(queues = SIMPLE_QUEUE) 监听，log 打印收到的消息

// === 2. WorkQueueDemo ===
// 路径: /work/send
// 生产: rabbitTemplate.convertAndSend(WORK_QUEUE, message);
// 消费: 两个 @RabbitListener 同时监听 WORK_QUEUE
//       Worker-1 处理耗时 1s，Worker-2 耗时 2s
//       观察: prefetch=1 时，处理快的 Worker 消费更多

// === 3. DirectDemo ===
// 路径: /direct/send?routingKey=xxx&message=xxx
// 生产: rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, routingKey, message);
// 消费: 两个消费者分别监听 direct.queue.1 和 direct.queue.2
//       routingKey=direct.key.1 → 只有 Queue1 收到
//       routingKey=direct.key.2 → 只有 Queue2 收到

// === 4. FanoutDemo ===
// 路径: /fanout/send?message=xxx
// 生产: rabbitTemplate.convertAndSend(FANOUT_EXCHANGE, "", message);
//       ⭐ Fanout 忽略 routing key，传空字符串
// 消费: 两个消费者分别监听 fanout.queue.1 和 fanout.queue.2
//       两条队列都会收到相同消息

// === 5. TopicDemo ===
// 路径: /topic/send?routingKey=xxx&message=xxx
// 生产: rabbitTemplate.convertAndSend(TOPIC_EXCHANGE, routingKey, message);
// 消费: 三个消费者分别监听 topic.queue.1/2/3
//       绑定规则: *.orange.* / *.*.rabbit / lazy.#
//       测试用例:
//         "quick.orange.rabbit" → Queue1 + Queue2
//         "lazy.orange.rabbit"  → Queue1 + Queue2 + Queue3
//         "quick.orange.fox"    → Queue1
//         "lazy.brown.fox"      → Queue3

// === 6. TtlDemo ===
// 路径: /ttl/send?message=xxx
// 生产: convertAndSend(TTL_EXCHANGE, TTL_ROUTING_KEY, message);
//       队列配置了 x-message-ttl=10000ms，10秒后消息过期进入死信队列
// 消费: 监听 DLX_QUEUE（死信队列），打印过期消息
//       不要消费 TTL_QUEUE（让消息过期）
// 扩展: /ttl/send-delay?message=xxx&delayMs=5000（消息级别 TTL）

// === 7. DelayDemo ===
// 路径: /delay/send?message=xxx&delayMs=5000
// 生产: convertAndSend(DELAY_EXCHANGE, "delay.key", message, msg -> {
//           msg.getMessageProperties().setExpiration(String.valueOf(delayMs));
//           return msg;
//       });
// 消费: 监听 DLX_QUEUE（复用死信队列）
// 场景: 模拟订单 30 分钟未支付自动取消

// === 8. ConfirmDemo ===
// 路径: /confirm/send?message=xxx
// 生产: convertAndSend(CONFIRM_EXCHANGE, CONFIRM_ROUTING_KEY, message);
//       发送成功会触发 confirmCallback（在 RabbitMQConfig 中已配置）
// 消费: 监听 CONFIRM_QUEUE
// 扩展: /confirm/send-fail — 发送到不存在的 exchange，观察 nack 回调

// === 9. PriorityDemo ===
// 路径: /priority/send?message=xxx&priority=5
// 生产: convertAndSend(PRIORITY_QUEUE, message, msg -> {
//           msg.getMessageProperties().setPriority(priority);
//           return msg;
//       });
// 消费: 监听 PRIORITY_QUEUE
// 测试: 先发多条 priority=0，再发 priority=5 和 10，观察消费顺序
```

---

## 模块六：elasticsearch-practice（端口 8084）

### 前置条件
- 本地启动 Elasticsearch（默认 localhost:9200）
- 安装 ik 分词器插件（Article 实体用了 ik_max_word）

### 任务清单

| 序号 | 要写的类 | 练习要点 |
|------|---------|---------|
| 1 | `config/ESConfig.java` | ElasticsearchRestTemplate 配置，自定义 ObjectMapper |
| 2 | `config/Knife4jConfig.java` | OpenAPI 描述元数据（参考其他模块写法） |
| 3 | `repository/ArticleRepository.java` | 继承 `ElasticsearchRepository<Article, String>`，自定义方法 |
| 4 | `repository/ESUserRepository.java` | 继承 `ElasticsearchRepository<ESUser, String>` |
| 5 | `service/ArticleService.java` | **核心练习类**，见下方详细说明 |
| 6 | `service/ESUserService.java` | **核心练习类**，见下方详细说明 |
| 7 | `controller/ArticleController.java` | REST 接口，聚合 Service，Swagger 注解 |
| 8 | `controller/ESUserController.java` | REST 接口 |

### ArticleService 需要实现的方法

```java
// 基础 CRUD
Article save(Article article);                          // 索引文档
Article getById(String id);                             // 根据 ID 获取
void deleteById(String id);                             // 删除
List<Article> saveBatch(List<Article> articles);        // 批量索引

// 全文检索
Page<Article> searchByKeyword(String keyword, int page, int size);     // 关键词搜索 (MultiMatchQuery)
Page<Article> searchByTitle(String title, int page, int size);         // 标题搜索
Page<Article> searchByAuthorAndCategory(String author, String category, int page, int size);  // 精确匹配

// 高级查询
Page<Article> searchByScoreRange(Double minScore, Double maxScore, int page, int size);  // 范围查询
Page<Article> searchByTimeRange(LocalDateTime start, LocalDateTime end, int page, int size);
Page<Article> searchWithHighlight(String keyword, int page, int size);   // ⭐ 高亮查询

// 聚合
Map<String, Long> aggregateByCategory();     // 按分类聚合统计
Map<String, Long> aggregateByAuthor();       // 按作者聚合统计
```

### ESUserService 需要实现的方法

```java
// 基础 CRUD
ESUser save(ESUser user);
ESUser getById(String id);
void deleteById(String id);
List<ESUser> saveBatch(List<ESUser> users);

// 查询
Page<ESUser> searchByNickname(String nickname, int page, int size);   // 昵称分词查询
Page<ESUser> searchByCity(String city, int page, int size);           // 精确匹配
Page<ESUser> searchByAgeRange(Integer minAge, Integer maxAge, int page, int size);  // 范围查询
Page<ESUser> searchBySalaryRange(Double minSalary, Double maxSalary, int page, int size);

// 排序
Page<ESUser> searchByCityOrderBySalary(String city, boolean asc, int page, int size);
```

---

## 模块七：xxl-job-practice（端口 8085）

### 前置条件
- 需要 XXL-JOB 调度中心（xxl-job-admin），可从 GitHub 下载源码运行，或用 Docker
- Docker 快速启动: `docker run -p 8080:8080 xuxueli/xxl-job-admin:2.4.1`

### 任务清单

| 序号 | 要写的类 | 练习要点 |
|------|---------|---------|
| 1 | `config/Knife4jConfig.java` | OpenAPI 配置 |
| 2 | `job/SimpleJobHandler.java` | **基础任务** — 最简单的定时任务 |
| 3 | `job/ParamJobHandler.java` | **参数化任务** — 从任务参数获取执行参数 |
| 4 | `job/ShardingJobHandler.java` | **分片广播** — 多实例分片处理大数据 |
| 5 | `job/CronJobHandler.java` | **CRON 表达式** — 演示各种 CRON 调度 |
| 6 | `controller/JobController.java` | 提供手动触发任务的接口 |

### 各 JobHandler 详细说明

```java
// SimpleJobHandler — 最基础的 Bean 模式任务
@XxlJob("simpleJobHandler")
public void execute() throws Exception {
    // 1. 执行业务逻辑（比如：每天统计一次用户注册数）
    // 2. 用 XxlJobHelper.log() 记录执行日志
    // 3. 用 XxlJobHelper.handleSuccess/handleFail 返回执行结果
}

// ParamJobHandler — 带参数任务
@XxlJob("paramJobHandler")
public void execute() {
    // 1. 用 XxlJobHelper.getJobParam() 获取任务参数
    // 2. 解析参数，执行对应逻辑（如：参数指定要清理哪张表）
}

// ShardingJobHandler — 分片广播任务
@XxlJob("shardingJobHandler")
public void execute() {
    // 1. 用 XxlJobHelper.getShardIndex() 获取当前分片序号
    // 2. 用 XxlJobHelper.getShardTotal() 获取分片总数
    // 3. 按 分片序号 % 总数 分配数据处理范围
}

// CronJobHandler — 不同 CRON 表达式的任务
@XxlJob("cronJobHandler")
public void execute() {
    // 这个任务本身很简单，重点是在 XXL-JOB Admin 里配置不同的 CRON 表达式：
    // 秒 分 时 日 月 周
    // 每5秒:    */5 * * * * ?
    // 每天0点:   0 0 0 * * ?
    // 每周一:    0 0 0 ? * MON
    // 每月1号:   0 0 0 1 * ?
}
```

---

## 模块八：sc-nacos-practice（端口 8086）

### 前置条件
- 本地启动 Nacos（默认 localhost:8848）
- `docker run -p 8848:8848 -e MODE=standalone nacos/nacos-server:v2.3.1`

### 任务清单

| 序号 | 要写的类 | 练习要点 |
|------|---------|---------|
| 1 | `config/Knife4jConfig.java` | OpenAPI 配置 |
| 2 | `config/NacosConfigRefresh.java` | **@RefreshScope 配置热更新** |
| 3 | `service/DiscoveryService.java` | **服务发现** — 从 Nacos 获取服务实例列表 |
| 4 | `feign/UserFeignClient.java` | **Feign 远程调用** — 定义远程接口 |
| 5 | `controller/ConfigController.java` | 演示配置中心：读取配置、热更新 |
| 6 | `controller/DiscoveryController.java` | 演示服务发现：获取实例列表 |
| 7 | `controller/FeignController.java` | 演示 Feign 调用其他服务 |

### 各类详细说明

```java
// NacosConfigRefresh — 配置热更新
@Component
@RefreshScope  // ⭐ 关键注解: Nacos 配置变更时自动刷新
public class NacosConfigRefresh {
    @Value("${custom.welcome:默认欢迎语}")
    private String welcomeMessage;

    @Value("${custom.pool-size:10}")
    private Integer poolSize;

    // 提供 getter 方法供 Controller 读取
}

// DiscoveryService — 服务发现
@Service
public class DiscoveryService {
    @Autowired
    private DiscoveryClient discoveryClient;

    // 获取所有服务名
    public List<String> getServices();

    // 获取指定服务的所有实例
    public List<ServiceInstance> getInstances(String serviceName);

    // 获取实例数量
    public int getInstanceCount(String serviceName);
}

// UserFeignClient — Feign 远程调用
@FeignClient(name = "nacos-practice", fallbackFactory = UserFeignClientFallback.class)
public interface UserFeignClient {
    @GetMapping("/api/config/welcome")
    Result<String> getWelcome();

    @GetMapping("/api/discovery/services")
    Result<List<String>> getServices();
}

// 降级工厂 (服务不可用时的兜底逻辑)
@Component
public class UserFeignClientFallback implements FallbackFactory<UserFeignClient> {
    @Override
    public UserFeignClient create(Throwable cause) {
        return new UserFeignClient() { /* 返回降级数据 */ };
    }
}
```

---

## 模块九：sc-sentinel-practice（端口 8087）

### 前置条件
- 可选：启动 Sentinel Dashboard（Docker: `docker run -p 8858:8858 bladex/sentinel-dashboard:1.8.7`）
- 不启动 Dashboard 也可以通过代码配置规则

### 任务清单

| 序号 | 要写的类 | 练习要点 |
|------|---------|---------|
| 1 | `config/Knife4jConfig.java` | OpenAPI 配置 |
| 2 | `config/SentinelRuleConfig.java` | **代码配置流控规则** (可选，也可以在 Dashboard 配) |
| 3 | `service/FlowLimitService.java` | **QPS 流控** — @SentinelResource + 流控降级 |
| 4 | `service/DegradeService.java` | **熔断降级** — 模拟慢调用/异常比例触发熔断 |
| 5 | `service/HotParamService.java` | **热点参数限流** — 按参数值限流（如 userId 限流） |
| 6 | `controller/FlowController.java` | 流控演示接口 |
| 7 | `controller/DegradeController.java` | 熔断降级演示接口 |
| 8 | `controller/HotParamController.java` | 热点参数限流演示接口 |

### 各 Service 详细说明

```java
// FlowLimitService — QPS 流控
@Service
public class FlowLimitService {
    // ⭐ value 是资源名，blockHandler 是被限流后走的方法
    @SentinelResource(value = "flowLimit", blockHandler = "flowLimitBlock")
    public String flowLimit() {
        return "正常请求通过";
    }

    // blockHandler 方法签名必须和原方法一致，额外加 BlockException
    public String flowLimitBlock(BlockException ex) {
        return "被限流了！规则: " + ex.getRule();
    }
}

// DegradeService — 熔断降级
@Service
public class DegradeService {
    // 模拟调用外部服务（可能慢、可能抛异常）
    @SentinelResource(value = "callRemote", fallback = "callRemoteFallback")
    public String callRemote(boolean slow, boolean error) {
        if (slow) Thread.sleep(1000);  // 慢调用 (配合慢调用比例熔断)
        if (error) throw new RuntimeException("服务异常");
        return "调用成功";
    }

    // fallback: 业务异常时的降级
    public String callRemoteFallback(boolean slow, boolean error, Throwable t) {
        return "降级处理: " + t.getMessage();
    }
}

// HotParamService — 热点参数限流
@Service
public class HotParamService {
    // paramIndex=0 表示对第一个参数限流
    // 在 Dashboard 配置热点规则: 参数值=特定值时限流阈值更低
    @SentinelResource(value = "hotParam", blockHandler = "hotParamBlock")
    public String queryByUserId(Long userId, String query) {
        return "查询用户 " + userId + " 的数据";
    }

    public String queryByUserIdBlock(Long userId, String query, BlockException ex) {
        return "用户 " + userId + " 被限流";
    }
}
```

---

## 模块十：sc-gateway-practice（端口 9000）

### 前置条件
- 需要 Nacos（Gateway 从 Nacos 发现微服务）
- 先把其他微服务（如 nacos-practice）注册到 Nacos，Gateway 才能路由过去

### 任务清单

| 序号 | 要写的类 | 练习要点 |
|------|---------|---------|
| 1 | `config/CorsConfig.java` | **跨域配置**（Java 代码方式，备选） |
| 2 | `filter/LogFilter.java` | **全局过滤器** — 记录请求日志（IP、路径、耗时） |
| 3 | `filter/AuthFilter.java` | **认证过滤器** — 校验 Token，拦截未登录请求 |
| 4 | `config/DynamicRouteConfig.java` | **动态路由** — 从 Nacos 配置读取路由规则（进阶） |
| 5 | 在 `application.yml` 中配置路由 | 基础路由配置 |

### 各类详细说明

```java
// LogFilter — 全局日志过滤器
@Component
public class LogFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 记录请求开始时间
        // 2. 获取请求 IP、路径、方法
        // 3. chain.filter() 继续执行
        // 4. 记录响应状态码和耗时
        // ⭐ 注意: Gateway 基于 WebFlux，用 ServerWebExchange 不是 HttpServletRequest
    }

    @Override
    public int getOrder() {
        return -1;  // 数字越小优先级越高
    }
}

// AuthFilter — 认证过滤器
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 判断是否白名单路径（如 /login, /public/**）→ 放行
        // 2. 从 Header 或 Query 取 Token
        // 3. 校验 Token 有效性
        // 4. 无效 → 返回 401 JSON 响应
        // 5. 有效 → chain.filter() 继续
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

// application.yml 路由配置（你需要补充）
// 知识点:
//   - id: 路由唯一标识
//   - uri: lb://服务名 (从 Nacos 发现) 或固定 URL
//   - predicates: Path / After / Before / Between / Header / Method / Query
//   - filters: StripPrefix / AddHeader / RewritePath / RequestRateLimiter
```

---

## 完成顺序建议

### 第一阶段：基础中间件（无外部依赖，启动即用）
1. **mybatis-plus-practice** — H2 内存数据库，无需安装任何东西，最先做
2. **redis-practice** — 需要启动 Redis，最常用的中间件

### 第二阶段：消息队列 + 搜索
3. **rabbitmq-practice** — 需要启动 RabbitMQ
4. **elasticsearch-practice** — 需要启动 ES + ik 分词器

### 第三阶段：分布式任务调度
5. **xxl-job-practice** — 需要 XXL-JOB Admin

### 第四阶段：微服务全家桶（按顺序）
6. **sc-nacos-practice** — 先跑起来，后面两个模块依赖它
7. **sc-sentinel-practice** — 依赖 Nacos 注册（代码规则可单独跑）
8. **sc-gateway-practice** — 最后做，需要其他服务都注册到 Nacos

---

## 遇到问题怎么问我

- ❌ "帮我写完整代码" → ✅ "ArticleService 的高亮查询怎么写"
- ❌ "这个怎么实现" → ✅ "ElasticsearchRepository 自定义方法命名规则是什么"
- ❌ "全给我" → ✅ "先做第一个任务，写完你帮我检查"
