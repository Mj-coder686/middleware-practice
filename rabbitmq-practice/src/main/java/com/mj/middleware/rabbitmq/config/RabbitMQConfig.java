//package com.mj.middleware.rabbitmq.config;
//
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.mj.middleware.rabbitmq.config.RabbitMQConstants.*;
//
///**
// * RabbitMQ 配置类 — 声明所有交换机、队列、绑定关系
// *
// * 配置方式二选一：
// * 1. Java Config（本类）— 类型安全，推荐
// * 2. @RabbitListener(queues=...) 注解声明 — 简单但不集中
// */
//@Configuration
//public class RabbitMQConfig {
//
//    // ==================== 简单队列 ====================
//
//    @Bean
//    public Queue simpleQueue() {
//        // durable=true 持久化队列，exclusive=false 非排他，autoDelete=false 不自动删除
//        return QueueBuilder.durable(SIMPLE_QUEUE).build();
//    }
//
//    // ==================== 工作队列 ====================
//
//    @Bean
//    public Queue workQueue() {
//        return QueueBuilder.durable(WORK_QUEUE).build();
//    }
//
//    // ==================== Direct 交换机 ====================
//
//    @Bean
//    public DirectExchange directExchange() {
//        return ExchangeBuilder.directExchange(DIRECT_EXCHANGE).durable(true).build();
//    }
//
//    @Bean
//    public Queue directQueue1() {
//        return QueueBuilder.durable(DIRECT_QUEUE_1).build();
//    }
//
//    @Bean
//    public Queue directQueue2() {
//        return QueueBuilder.durable(DIRECT_QUEUE_2).build();
//    }
//
//    @Bean
//    public Binding directBinding1() {
//        return BindingBuilder.bind(directQueue1()).to(directExchange()).with(DIRECT_ROUTING_KEY_1);
//    }
//
//    @Bean
//    public Binding directBinding2() {
//        return BindingBuilder.bind(directQueue2()).to(directExchange()).with(DIRECT_ROUTING_KEY_2);
//    }
//
//    // ==================== Fanout 交换机 ====================
//
//    @Bean
//    public FanoutExchange fanoutExchange() {
//        return ExchangeBuilder.fanoutExchange(FANOUT_EXCHANGE).durable(true).build();
//    }
//
//    @Bean
//    public Queue fanoutQueue1() {
//        return QueueBuilder.durable(FANOUT_QUEUE_1).build();
//    }
//
//    @Bean
//    public Queue fanoutQueue2() {
//        return QueueBuilder.durable(FANOUT_QUEUE_2).build();
//    }
//
//    @Bean
//    public Binding fanoutBinding1() {
//        return BindingBuilder.bind(fanoutQueue1()).to(fanoutExchange());
//    }
//
//    @Bean
//    public Binding fanoutBinding2() {
//        return BindingBuilder.bind(fanoutQueue2()).to(fanoutExchange());
//    }
//
//    // ==================== Topic 交换机 ====================
//
//    @Bean
//    public TopicExchange topicExchange() {
//        return ExchangeBuilder.topicExchange(TOPIC_EXCHANGE).durable(true).build();
//    }
//
//    @Bean
//    public Queue topicQueue1() {
//        return QueueBuilder.durable(TOPIC_QUEUE_1).build();
//    }
//
//    @Bean
//    public Queue topicQueue2() {
//        return QueueBuilder.durable(TOPIC_QUEUE_2).build();
//    }
//
//    @Bean
//    public Queue topicQueue3() {
//        return QueueBuilder.durable(TOPIC_QUEUE_3).build();
//    }
//
//    @Bean
//    public Binding topicBinding1() {
//        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("*.orange.*");
//    }
//
//    @Bean
//    public Binding topicBinding2() {
//        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("*.*.rabbit");
//    }
//
//    @Bean
//    public Binding topicBinding3() {
//        return BindingBuilder.bind(topicQueue3()).to(topicExchange()).with("lazy.#");
//    }
//
//    // ==================== TTL 队列 + 死信队列 ====================
//
//    /**
//     * TTL 队列 — 消息 10 秒未被消费则进入死信队列
//     *
//     * 死信触发条件：
//     * 1. 消息被拒绝（basic.reject / basic.nack）且 requeue=false
//     * 2. 消息 TTL 过期
//     * 3. 队列达到最大长度（x-max-length）
//     */
//    @Bean
//    public Queue ttlQueue() {
//        Map<String, Object> args = new HashMap<>();
//        args.put("x-message-ttl", 10000);                  // 消息 TTL 10秒
//        args.put("x-dead-letter-exchange", DLX_EXCHANGE);   // 死信交换机
//        args.put("x-dead-letter-routing-key", DLX_ROUTING_KEY); // 死信路由键
//        return QueueBuilder.durable(TTL_QUEUE).withArguments(args).build();
//    }
//
//    @Bean
//    public DirectExchange ttlExchange() {
//        return ExchangeBuilder.directExchange(TTL_EXCHANGE).durable(true).build();
//    }
//
//    @Bean
//    public Binding ttlBinding() {
//        return BindingBuilder.bind(ttlQueue()).to(ttlExchange()).with(TTL_ROUTING_KEY);
//    }
//
//    /** 死信交换机 + 死信队列 */
//    @Bean
//    public DirectExchange dlxExchange() {
//        return ExchangeBuilder.directExchange(DLX_EXCHANGE).durable(true).build();
//    }
//
//    @Bean
//    public Queue dlxQueue() {
//        return QueueBuilder.durable(DLX_QUEUE).build();
//    }
//
//    @Bean
//    public Binding dlxBinding() {
//        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(DLX_ROUTING_KEY);
//    }
//
//    // ==================== 延迟队列（消息级别 TTL） ====================
//
//    /**
//     * 延迟队列 — 每条消息设置不同 TTL
//     * 注意：RabbitMQ 不支持消息级别 TTL 排序，可能有"队头阻塞"问题
//     * 生产推荐使用 rabbitmq_delayed_message_exchange 插件
//     */
//    @Bean
//    public Queue delayQueue() {
//        Map<String, Object> args = new HashMap<>();
//        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
//        args.put("x-dead-letter-routing-key", DLX_ROUTING_KEY);
//        return QueueBuilder.durable(DELAY_QUEUE).withArguments(args).build();
//    }
//
//    @Bean
//    public DirectExchange delayExchange() {
//        return ExchangeBuilder.directExchange(DELAY_EXCHANGE).durable(true).build();
//    }
//
////    @Bean
////    public Binding delayBinding() {
////        return BindingBuilder.bind(delayQueue()).to(DELAY_EXCHANGE).with("delay.key");
////    }
//
//    // ==================== 优先级队列 ====================
//
//    @Bean
//    public Queue priorityQueue() {
//        Map<String, Object> args = new HashMap<>();
//        args.put("x-max-priority", 10);  // 最大优先级 10
//        return QueueBuilder.durable(PRIORITY_QUEUE).withArguments(args).build();
//    }
//
//    // ==================== 发布确认 ====================
//
//    @Bean
//    public DirectExchange confirmExchange() {
//        return ExchangeBuilder.directExchange(CONFIRM_EXCHANGE).durable(true).build();
//    }
//
//    @Bean
//    public Queue confirmQueue() {
//        return QueueBuilder.durable(CONFIRM_QUEUE).build();
//    }
//
//    @Bean
//    public Binding confirmBinding() {
//        return BindingBuilder.bind(confirmQueue()).to(confirmExchange()).with(CONFIRM_ROUTING_KEY);
//    }
//
//    // ==================== 回退消息 + 备份交换机 ====================
//
//    @Bean
//    public DirectExchange returnExchange() {
//        // 设置备份交换机
//        Map<String, Object> args = new HashMap<>();
//        args.put("alternate-exchange", BACKUP_EXCHANGE);
//        return (DirectExchange) ExchangeBuilder.directExchange(RETURN_EXCHANGE)
//                .durable(true).withArguments(args).build();
//    }
//
//    @Bean
//    public Queue returnQueue() {
//        return QueueBuilder.durable(RETURN_QUEUE).build();
//    }
//
//    @Bean
//    public Binding returnBinding() {
//        return BindingBuilder.bind(returnQueue()).to(returnExchange()).with("return.key");
//    }
//
//    /** 备份交换机（Fanout 类型） */
//    @Bean
//    public FanoutExchange backupExchange() {
//        return ExchangeBuilder.fanoutExchange(BACKUP_EXCHANGE).durable(true).build();
//    }
//
//    @Bean
//    public Queue backupQueue() {
//        return QueueBuilder.durable(BACKUP_QUEUE).build();
//    }
//
//    @Bean
//    public Queue warningQueue() {
//        return QueueBuilder.durable(WARNING_QUEUE).build();
//    }
//
//    @Bean
//    public Binding backupBinding() {
//        return BindingBuilder.bind(backupQueue()).to(backupExchange());
//    }
//
//    @Bean
//    public Binding warningBinding() {
//        return BindingBuilder.bind(warningQueue()).to(backupExchange());
//    }
//
//    // ==================== RabbitTemplate 配置 ====================
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate template = new RabbitTemplate(connectionFactory);
//        template.setMandatory(true);  // 开启回退消息
//
//        // 发布确认回调
//        template.setConfirmCallback((correlationData, ack, cause) -> {
//            if (ack) {
//                System.out.println("[Confirm] 消息发送成功: " + (correlationData != null ? correlationData.getId() : ""));
//            } else {
//                System.out.println("[Confirm] 消息发送失败: " + cause);
//            }
//        });
//
//        // 回退消息回调（消息无法路由到任何队列时触发）
//        template.setReturnsCallback(returned -> {
//            System.out.println("[Return] 消息被退回: exchange=" + returned.getExchange()
//                    + ", routingKey=" + returned.getRoutingKey()
//                    + ", replyText=" + returned.getReplyText());
//        });
//
//        return template;
//    }
//
//}
