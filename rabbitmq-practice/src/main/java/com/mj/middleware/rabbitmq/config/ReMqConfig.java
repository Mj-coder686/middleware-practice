package com.mj.middleware.rabbitmq.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReMqConfig {

    public DirectExchange errorExchange() {
        return new DirectExchange("error.exchange");
    }
    public Queue errorQueue() {
        return new Queue("error.queue");
    }
    public Binding errorBinding() {
        return BindingBuilder.bind(errorQueue()).to(errorExchange()).with("error.routing.key");
    }
}
