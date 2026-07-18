package com.mj.middleware.rabbitmq.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class mq {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter()); // 强制使用 JSON 序列化器
        return rabbitTemplate;
    }

    @Bean
    public DirectExchange orderDirectexchange() {
        return new DirectExchange(OrderConstants.ORDER_DIRECT_EXCHANGE , true , false);
    }

    @Bean
    public Queue pointsReleaseQueue() {
        return new Queue(OrderConstants.POINTS_RELEASE_QUEUE , true);
    }

    @Bean
    public Binding pointReleaseBinding(){
        return BindingBuilder
                .bind(pointsReleaseQueue())
                .to(orderDirectexchange())
                .with(OrderConstants.ORDER_PAY_SUCCESS_ROUTING_KEY);
    }

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                // 1. 先拿到发送消息时绑定的“关联数据”
                String msgId = correlationData != null ? correlationData.getId() : "未知ID";

                if (ack) {
                    // ack = true，说明 Exchange 已经收到消息并给 Java 确认了
                    log.info("【发送者确认】🎉 消息已成功送达交换机！消息ID: {}", msgId);
                } else {
                    // ack = false，说明 MQ 拒绝接收，或者网络断了
                    log.error("【发送者确认】❌ 消息投递到交换机失败！！！消息ID: {}，原因: {}", msgId, cause);

                    // 🚨【生产环境关键细节】：
                    // 如果到这里失败了，说明消息根本没有进入 MQ。
                    // 1. 如果是高频交易，千万不要盲目无限死循环重发，会把内存挤爆。
                    // 2. 推荐做法：把发送失败的消息 ID、内容和失败原因，直接记录到数据库的“消息投递失败表”中。
                    // 3. 开启一个定时任务，每隔一分钟扫一次失败表，进行人工复核或者有限次数的重新投递。
                }
            }
        });
        // ==================== 🛡️ 守卫二：ReturnsCallback ====================
        // 它的唯一使命：当消息到了 Exchange，但“无法路由到任何 Queue”时，触发退回。
        // 细节：如果消息顺利路由到 Queue 了，这个方法【绝对不会】被调用！
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("【消息路由失败退回】💀 警告：Exchange 无法将消息路由到任何队列！");
            log.error("【退回详情】交换机: {} | 路由键: {} | 错误回应码: {} | 原因说明: {}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyCode(),
                    returned.getReplyText());
            log.error("【退回数据】{}", new String(returned.getMessage().getBody()));

            // 🚨【生产环境关键细节】：
            // 路由失败 100% 是配置问题（比如你拼写错了 RoutingKey，或者代码里漏写了 Queue 的绑定）。
            // 此时消息已经从 MQ 里被弹出来了，如果不处理，这条消息就彻底蒸发了！
            // 推荐做法：在代码里加入即时通讯（如企业微信/钉钉群机器人/邮件）的报警 API，只要触发这个回调，立刻在开发群里炸锅，通知程序员修复代码！
        });
    }
}
