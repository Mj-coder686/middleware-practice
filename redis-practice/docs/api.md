# Redis 练习模块 — API 文档

> 启动端口：8082
> Swagger UI：http://localhost:8082/doc.html
> 前置条件：需要本地启动 Redis（docker run -d -p 6379:6379 redis:7）

---

## String 操作

### 设置值
```
POST /redis/string/set?key=name&value=张三
```

### 获取值
```
GET /redis/string/get?key=name
```

### 设置带过期时间
```
POST /redis/string/setex?key=token&value=abc123&seconds=3600
```

### 计数器自增
```
POST /redis/string/incr?key=page:views
# 每次调用 +1，返回当前值
```

---

## Hash 操作

### 设置字段
```
POST /redis/hash/set?key=user:1&field=name&value=张三
```

### 获取全部字段
```
GET /redis/hash/all?key=user:1
```

### 购物车：添加商品
```
POST /redis/hash/cart/add?userId=1001&productId=P001&quantity=2
```

### 购物车：查看
```
GET /redis/hash/cart?userId=1001
# 返回 { "P001": "2", "P002": "1" }
```

---

## List 操作

### 入队列（右推入）
```
POST /redis/list/push?key=mq:order&value=订单消息1
POST /redis/list/push?key=mq:order&value=订单消息2
```

### 出队列（左弹出）
```
POST /redis/list/pop?key=mq:order
# 返回 "订单消息1"（先进先出）
```

### 获取范围
```
GET /redis/list/range?key=mq:order&start=0&end=-1
# -1 表示到最后
```

---

## Set 操作

### 添加元素
```
POST /redis/set/add?key=follow:1001&value=1002
POST /redis/set/add?key=follow:1001&value=1003
POST /redis/set/add?key=follow:1002&value=1003
```

### 获取所有成员
```
GET /redis/set/members?key=follow:1001
```

### 共同好友
```
GET /redis/set/common?user1=1001&user2=1002
# 返回两个用户共同关注的人
```

---

## ZSet 操作

### 添加元素（排行榜）
```
POST /redis/zset/add?key=leaderboard&member=player1&score=100
POST /redis/zset/add?key=leaderboard&member=player2&score=200
POST /redis/zset/add?key=leaderboard&member=player3&score=150
```

### 增加分数
```
POST /redis/zset/incr?key=leaderboard&member=player1&delta=50
```

### 获取 Top N
```
GET /redis/zset/top?key=leaderboard&count=10
# 返回降序排名前 10
```

---

## 分布式锁

### 模拟秒杀
```
POST /redis/lock/seckill?productId=P001
# 模拟并发安全的库存扣减
# 多次调用观察库存递减
```

---

## 缓存策略

### Cache Aside 读操作
```
GET /redis/cache/aside?key=product:1
# 首次调用：缓存未命中，从 DB 加载并回填
# 再次调用：缓存命中，直接返回
```
