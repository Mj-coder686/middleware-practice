# RabbitMQ 学习指南

## 一、概述

**RabbitMQ** 是基于 AMQP 协议的开源消息中间件，由 Erlang 开发，支持高可用、灵活路由、消息确认等特性。

**核心概念：**
- **Producer** — 消息生产者
- **Exchange** — 交换机，负责路由消息到队列
- **Queue** — 消息存储队列
- **Consumer** — 消息消费者
- **Binding** — 交换机与队列的绑定关系（含 routing key）

## 二、环境搭建

### Docker 启动

```bash
# 启动 RabbitMQ（含管理界面）
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management

# 管理界面：http://localhost:15672（guest/guest）
```

### 依赖配置

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### application.yml

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated  # 发布确认
    publisher-returns: true             # 回退消息
    listener:
      simple:
        prefetch: 1                     # 公平分发
        acknowledge-mode: manual        # 手动 ACK
```

## 三、核心知识点

### 🟢 必须掌握

#### 1. 四种交换机类型

| 类型 | 路由规则 | 适用场景 |
|------|----------|----------|
| **Direct** | routing key 精确匹配 | 定向路由、日志分级 |
| **Fanout** | 广播到所有绑定队列 | 事件广播、通知 |
| **Topic** | 通配符匹配（*一个词，#多个词） | 主题订阅、消息分类 |
| **Headers** | 消息 header 匹配（少用） | 复杂路由条件 |

**对应代码：** `direct/DirectDemo.java`, `fanout/FanoutDemo.java`, `topic/TopicDemo.java`

#### 2. 消息确认机制

```
Producer → [Publisher Confirm] → Exchange → [Return Callback] → Queue → [Consumer ACK] → Consumer
```

- **Publisher Confirm** — Broker 确认收到消息
- **Return Callback** — 消息无法路由到队列时回调
- **Consumer ACK** — 消费者确认消费完成
  - `auto` — 自动确认（可能丢消息）
  - `manual` — 手动确认（推荐：`channel.basicAck()`）
  - `none` — 不确认

**对应代码：** `config/RabbitMQConfig.java`（RabbitTemplate 配置）

#### 3. 持久化

- **队列持久化** — `durable=true`（声明时设置）
- **消息持久化** — `deliveryMode=2`（PERSISTENT）
- **交换机持久化** — `durable=true`

---

### 🟡 进阶掌握

#### 4. TTL + 死信队列

```
Producer → TTL Queue (TTL=10s) → 过期 → DLX Exchange → DLX Queue → Consumer
```

**死信触发条件：**
1. 消息被拒绝（reject/nack）且 requeue=false
2. 消息 TTL 过期
3. 队列达到最大长度（x-max-length）

**对应代码：** `ttl/TtlDemo.java`

#### 5. 延迟队列

**方案对比：**

| 方案 | 优点 | 缺点 |
|------|------|------|
| TTL + DLX | 简单，无需插件 | 队头阻塞问题 |
| 延迟插件 | 无队头阻塞 | 需安装插件 |

**对应代码：** `delay/DelayDemo.java`

#### 6. 消费端限流与重试

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        prefetch: 1        # 每次只取 1 条
        acknowledge-mode: manual
        retry:
          enabled: true
          max-attempts: 3
```

#### 7. 消费者手动 ACK

```java
@RabbitListener(queues = "my.queue")
public void consume(Message message, Channel channel) throws IOException {
    long deliveryTag = message.getMessageProperties().getDeliveryTag();
    try {
        // 处理业务
        channel.basicAck(deliveryTag, false);      // 确认
    } catch (Exception e) {
        channel.basicNack(deliveryTag, false, true); // 拒绝并重新入队
        // channel.basicReject(deliveryTag, false);  // 拒绝丢弃
    }
}
```

---

### 🔴 高级/面试常问

#### 8. 消息可靠性保证（全链路）

```
Producer Confirm → Exchange 持久化 → Queue 持久化 → Consumer 手动 ACK
```

任一环节出问题的应对：
- **发送失败** → confirmCallback 通知生产者重试
- **路由失败** → returnCallback + 备份交换机
- **Broker 宕机** → 队列+消息持久化，重启后恢复
- **消费失败** → 手动 nack + 重试 / 死信队列

#### 9. 常见面试题

**Q: 如何保证消息不丢失？**
A: 三端保证：生产者开启 confirm、队列+消息持久化、消费者手动 ACK。

**Q: 如何保证消息不重复消费（幂等性）？**
A: 消费端做幂等处理：数据库唯一键、Redis setNX、状态机。

**Q: 如何处理消息积压？**
A: 增加消费者数量 → 增加队列数量 → 临时扩容消费者 → 紧急降级。

**Q: 死信队列和延迟队列的区别？**
A: 死信队列是被动触发（过期/拒绝/满），延迟队列是主动延迟投递。延迟队列可以用死信实现，但推荐用延迟插件。

## 四、最佳实践

1. **消息必须持久化** — 队列、交换机、消息都设置 durable
2. **开启发布确认** — `publisher-confirm-type: correlated`
3. **消费者手动 ACK** — 业务处理成功后再确认
4. **设置 prefetch** — 防止消费者被大量消息压垮
5. **消息体不要太大** — 建议 < 1MB，大消息传文件路径
6. **死信队列兜底** — 消费失败的消息进入死信队列，人工处理

## 五、参考链接

- [RabbitMQ 官方文档](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP 文档](https://docs.spring.io/spring-amqp/reference/)
- [RabbitMQ 延迟插件](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange)
