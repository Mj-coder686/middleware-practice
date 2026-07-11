package com.mj.middleware.rabbitmq.config;

/**
 * RabbitMQ 常量定义 — 交换机、队列、路由键
 *
 * RabbitMQ 核心概念：
 * Producer → Exchange → Binding → Queue → Consumer
 *
 * 四种交换机类型：
 * 1. Direct  — 精确匹配 routing key
 * 2. Fanout  — 广播到所有绑定队列（忽略 routing key）
 * 3. Topic   — 通配符匹配 routing key（* 匹配一个词，# 匹配零或多个词）
 * 4. Headers — 根据消息 header 匹配（少用）
 */
public class RabbitMQConstants {

    // ==================== 简单队列 ====================
    public static final String SIMPLE_QUEUE = "simple.queue";

    // ==================== 工作队列 ====================
    public static final String WORK_QUEUE = "work.queue";

    // ==================== Direct 交换机 ====================
    public static final String DIRECT_EXCHANGE = "direct.exchange";
    public static final String DIRECT_QUEUE_1 = "direct.queue.1";
    public static final String DIRECT_QUEUE_2 = "direct.queue.2";
    public static final String DIRECT_ROUTING_KEY_1 = "direct.key.1";
    public static final String DIRECT_ROUTING_KEY_2 = "direct.key.2";

    // ==================== Fanout 交换机 ====================
    public static final String FANOUT_EXCHANGE = "fanout.exchange";
    public static final String FANOUT_QUEUE_1 = "fanout.queue.1";
    public static final String FANOUT_QUEUE_2 = "fanout.queue.2";

    // ==================== Topic 交换机 ====================
    public static final String TOPIC_EXCHANGE = "topic.exchange";
    public static final String TOPIC_QUEUE_1 = "topic.queue.1";   // *.orange.*
    public static final String TOPIC_QUEUE_2 = "topic.queue.2";   // *.*.rabbit
    public static final String TOPIC_QUEUE_3 = "topic.queue.3";   // lazy.#

    // ==================== TTL + 死信队列 ====================
    public static final String TTL_EXCHANGE = "ttl.exchange";
    public static final String TTL_QUEUE = "ttl.queue";           // 10s TTL
    public static final String TTL_ROUTING_KEY = "ttl.key";

    public static final String DLX_EXCHANGE = "dlx.exchange";     // 死信交换机
    public static final String DLX_QUEUE = "dlx.queue";           // 死信队列
    public static final String DLX_ROUTING_KEY = "dlx.key";

    // ==================== 延迟队列（插件版） ====================
    public static final String DELAY_EXCHANGE = "delay.exchange";
    public static final String DELAY_QUEUE = "delay.queue";

    // ==================== 优先级队列 ====================
    public static final String PRIORITY_QUEUE = "priority.queue";

    // ==================== 发布确认 ====================
    public static final String CONFIRM_EXCHANGE = "confirm.exchange";
    public static final String CONFIRM_QUEUE = "confirm.queue";
    public static final String CONFIRM_ROUTING_KEY = "confirm.key";

    // ==================== 回退消息 ====================
    public static final String RETURN_EXCHANGE = "return.exchange";
    public static final String RETURN_QUEUE = "return.queue";
    public static final String BACKUP_EXCHANGE = "backup.exchange";   // 备份交换机
    public static final String BACKUP_QUEUE = "backup.queue";
    public static final String WARNING_QUEUE = "warning.queue";       // 报警队列
}
