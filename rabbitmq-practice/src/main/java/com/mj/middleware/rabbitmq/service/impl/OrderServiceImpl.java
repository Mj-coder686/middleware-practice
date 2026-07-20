package com.mj.middleware.rabbitmq.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.middleware.rabbitmq.config.DelayedMqConfig;
import com.mj.middleware.rabbitmq.config.OrderConstants;
import com.mj.middleware.rabbitmq.entity.OrderMP;
import com.mj.middleware.rabbitmq.mapper.OrderMapper;
import com.mj.middleware.rabbitmq.service.IOrderService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.UUID;

/**
 * 订单 Service 实现
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderMP> implements IOrderService {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public OrderMP get(Long id) {
        return getById(id);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createOrder(OrderMP order) {
        // 1. 完善订单信息并落库
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 20))
                .setStatus(1); // 1-已支付
        save(order);
        System.out.println("订单创建成功: " + order);
        HashMap<String, Object> msg = new HashMap<>();

        msg.put("orderId", order.getId());
        msg.put("userId", order.getUserId());
        int pointValue = order.getTotalAmount().intValue() / 10;
        msg.put("pointValue", pointValue);
//        rabbitTemplate.convertAndSend(
//                OrderConstants.ORDER_DIRECT_EXCHANGE,
//                OrderConstants.ORDER_PAY_SUCCESS_ROUTING_KEY,
//                msg
//        );
        rabbitTemplate.convertAndSend(
                DelayedMqConfig.DELAYED_EXCHANGE,
                DelayedMqConfig.DELAYED_ROUTING_KEY,
                msg,
                message -> {
                    message.getMessageProperties().setHeader("x-delay", 1000 * 60 * 5);
                    return message;
                }
        );
        System.out.println("消息发送成功: " + msg);
    }

    @Override
    public void updateOrder(Long id, OrderMP order) {
        OrderMP exist = getById(id);
        if (exist == null) {
            throw new RuntimeException("订单不存在: id=" + id);
        }
        if (order.getOrderNo() != null) exist.setOrderNo(order.getOrderNo());
        if (order.getUserId() != null) exist.setUserId(order.getUserId());
        if (order.getProductName() != null) exist.setProductName(order.getProductName());
        if (order.getQuantity() != null) exist.setQuantity(order.getQuantity());
        if (order.getTotalAmount() != null) exist.setTotalAmount(order.getTotalAmount());
        if (order.getStatus() != null) exist.setStatus(order.getStatus());
        updateById(exist);
    }

    @Override
    public void deleteOrder(Long id) {
        removeById(id);
    }

    @Override
    public IPage<OrderMP> listOrder(Integer pageNum, Integer pageSize) {
        Page<OrderMP> page = new Page<>(pageNum, pageSize);
        return lambdaQuery().page(page);
    }
}
