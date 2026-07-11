# RabbitMQ 练习模块 — API 文档

> 启动端口：8083
> RabbitMQ 管理界面：http://localhost:15672（guest/guest）
> 前置条件：docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management

---

## 简单队列 `/simple`

```
POST /simple/send?message=Hello RabbitMQ
# Consumer 日志：简单队列收到消息: Hello RabbitMQ
```

---

## 工作队列 `/work`

```
POST /work/send?message=任务1
POST /work/send?message=任务2
POST /work/send?message=任务3
# 观察 Worker-1 和 Worker-2 轮流消费
# 配置 prefetch=1 后，处理快的 Worker 消费更多
```

---

## Direct 交换机 `/direct`

```
# 发送到 Queue-1
POST /direct/send?routingKey=direct.key.1&message=Direct消息1

# 发送到 Queue-2
POST /direct/send?routingKey=direct.key.2&message=Direct消息2

# 发送到不存在的 key（消息丢失，触发 returnCallback）
POST /direct/send?routingKey=unknown.key&message=会丢失
```

---

## Fanout 交换机 `/fanout`

```
POST /fanout/send?message=广播消息
# Queue-1 和 Queue-2 都会收到
```

---

## Topic 交换机 `/topic`

```
# "quick.orange.rabbit" → Queue-1 (*.orange.*) + Queue-2 (*.*.rabbit)
POST /topic/send?routingKey=quick.orange.rabbit&message=匹配两个队列

# "lazy.orange.rabbit" → Queue-1 + Queue-2 + Queue-3 (lazy.#)
POST /topic/send?routingKey=lazy.orange.rabbit&message=匹配三个队列

# "lazy.brown.fox" → 只有 Queue-3 (lazy.#)
POST /topic/send?routingKey=lazy.brown.fox&message=只有Queue3

# "quick.orange.fox" → 只有 Queue-1 (*.orange.*)
POST /topic/send?routingKey=quick.orange.fox&message=只有Queue1
```

---

## TTL + 死信队列 `/ttl`

```
# 发送 TTL 消息，10秒后自动进入死信队列
POST /ttl/send?message=10秒后过期

# 发送自定义延迟消息
POST /ttl/send-delay?message=5秒延迟&delayMs=5000
POST /ttl/send-delay?message=20秒延迟&delayMs=20000
# 观察 Consumer 日志中死信队列的消费时间
```

---

## 延迟队列 `/delay`

```
# 模拟订单超时：5秒后取消
POST /delay/send?message=订单ORD001未支付&delayMs=5000

# 模拟延迟通知：10秒后发送
POST /delay/send?message=延迟通知&delayMs=10000
```

---

## 发布确认 `/confirm`

```
# 正常发送（触发 confirmCallback ack=true）
POST /confirm/send?message=正常消息

# 发送到不存在的交换机（触发 confirmCallback ack=false）
POST /confirm/send-fail?message=会确认失败
```

---

## 优先级队列 `/priority`

```
# 先发送多条低优先级消息
POST /priority/send?message=普通任务1&priority=0
POST /priority/send?message=普通任务2&priority=0
POST /priority/send?message=普通任务3&priority=0

# 再发送高优先级消息
POST /priority/send?message=紧急任务&priority=10

# 观察消费顺序：紧急任务最先被消费
```
