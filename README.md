# 中间件实践工程 (middleware-practice)

基于 **Spring Boot 3.2 + Java 21** 的中间件学习项目，涵盖数据库、缓存、消息队列、搜索、任务调度、微服务治理等主流中间件的核心用法与最佳实践。

## 项目结构

```
middleware-practice
├── middleware-common          # 公共模块（实体类、统一返回结果）
├── mybatis-plus-practice      # MyBatis-Plus
├── redis-practice             # Redis
├── rabbitmq-practice          # RabbitMQ
├── elasticsearch-practice     # Elasticsearch
├── xxl-job-practice           # XXL-JOB
├── sc-nacos-practice          # Nacos
├── sc-sentinel-practice       # Sentinel
└── sc-gateway-practice        # Spring Cloud Gateway
```

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 基础框架 |
| Java | 21 | JDK 版本 |
| Spring Cloud | 2023.0.1 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.0 | 阿里巴巴微服务组件 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| Redisson | 3.27.2 | Redis 客户端 |
| XXL-JOB | 2.4.1 | 分布式任务调度 |
| Knife4j | 4.5.0 | API 文档 |
| Hutool | 5.8.27 | Java 工具包 |
| Lombok | - | 简化代码 |

## 模块说明

### middleware-common
公共模块，提供各模块共用的实体类和统一返回结果封装。

### mybatis-plus-practice
MyBatis-Plus 快速开发实践：
- 基础 CRUD 操作
- 条件构造器（LambdaQueryWrapper）
- 分页查询
- 自动填充（createTime / updateTime）

### redis-practice
Redis 核心场景实践：
- 短信登录与 Session 共享
- 商户查询缓存（缓存穿透、缓存击穿、缓存雪崩解决方案）
- 优惠券秒杀（Redis + Lua 脚本实现库存扣减）
- 点赞与共同关注（Set 数据结构）
- Feed 流推送（推模式 / 拉模式）
- 签到打卡（BitMap）
- UV 统计（HyperLogLog）

### rabbitmq-practice
RabbitMQ 消息队列实践：
- 队列、交换机、绑定关系配置
- Work Queue 消息模型
- 发布确认（Publisher Confirm）
- 死信队列（DLX）
- 延迟队列

### elasticsearch-practice
Elasticsearch 搜索引擎实践：
- 索引创建与映射配置
- 文档 CRUD
- DSL 查询（match、bool、function_score）
- 高亮搜索

### xxl-job-practice
XXL-JOB 分布式任务调度实践：
- 执行器注册
- 任务配置（CRON、固定频率）
- 分片广播

### sc-nacos-practice
Nacos 服务注册与配置中心：
- 服务注册与发现
- 动态配置管理
- 配置热更新

### sc-sentinel-practice
Sentinel 流量控制与熔断降级：
- 流控规则（QPS、线程数）
- 降级规则（慢调用比例、异常比例）
- 热点参数限流
- 授权规则

### sc-gateway-practice
Spring Cloud Gateway 网关：
- 路由配置
- 断言（Predicate）与过滤器（Filter）
- 全局异常处理

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.12+
- Elasticsearch 8.x
- Nacos 2.3+
- XXL-JOB 2.4+

### 启动步骤

```bash
# 1. 克隆项目
git clone https://github.com/Mj-coder686/middleware-practice.git

# 2. 进入项目目录
cd middleware-practice

# 3. 编译整个项目
mvn clean install -DskipTests

# 4. 进入具体模块启动（以 redis-practice 为例）
cd redis-practice
mvn spring-boot:run
```

### 各模块端口

| 模块 | 端口 |
|------|------|
| mybatis-plus-practice | 8081 |
| redis-practice | 8082 |
| rabbitmq-practice | 8083 |
| elasticsearch-practice | 8084 |
| xxl-job-practice | 8085 |
| sc-nacos-practice | 8086 |
| sc-sentinel-practice | 8087 |
| sc-gateway-practice | 8080 |

> 端口号请以各模块 `application.yml` 中的实际配置为准。

## API 文档

集成了 Knife4j（Swagger），启动后访问：

```
http://localhost:{端口}/doc.html
```

## 项目收获

通过本项目可以系统掌握：
- 数据层：MyBatis-Plus CRUD、分页、自动填充
- 缓存层：Redis 数据结构实战、缓存三大问题解决方案、分布式锁
- 消息中间件：RabbitMQ 消息模型、可靠性投递、延迟队列
- 搜索引擎：Elasticsearch DSL 查询、高亮、聚合
- 任务调度：XXL-JOB 分布式定时任务
- 微服务治理：Nacos 注册中心与配置中心、Sentinel 流控熔断、Gateway 网关路由

## License

MIT
