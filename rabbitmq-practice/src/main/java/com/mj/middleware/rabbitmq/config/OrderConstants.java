package com.mj.middleware.rabbitmq.config;

public class OrderConstants {
   public static final String ORDER_DIRECT_EXCHANGE = "order.direct.exchange";

    // 异步发放积分队列
    public static final String POINTS_RELEASE_QUEUE = "points.release.queue";

    // 订单支付成功去加积分的路由键
    public static final String ORDER_PAY_SUCCESS_ROUTING_KEY = "order.pay.success.points";
}
