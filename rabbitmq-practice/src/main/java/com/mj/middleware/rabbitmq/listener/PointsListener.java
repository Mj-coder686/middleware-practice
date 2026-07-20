package com.mj.middleware.rabbitmq.listener;


import com.mj.middleware.rabbitmq.config.OrderConstants;
import com.mj.middleware.rabbitmq.entity.PointsMP;
import com.mj.middleware.rabbitmq.service.IPointsService;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PointsListener {
    @Autowired
    private IPointsService pointsService;


    /**
     * 监听积分发放队列
     */
    @RabbitListener(queues = OrderConstants.POINTS_RELEASE_QUEUE)
    public void onPointsReleaseMessage1(Map<String, Object> msg){
        try {
            Long orderId = Long.valueOf(msg.get("orderId").toString());
            Long userId = Long.valueOf(msg.get("userId").toString());
            Integer pointValue = Integer.valueOf(msg.get("pointValue").toString());
            System.out.println("【积分服务】开始处理积分发放消息：" + msg);

            // 如果积分小于等于0，不需要发放
            if (pointValue <= 0) {
                System.out.println("【积分服务】实付金额不足，赠送动积分为 0，跳过入库。");
                return;
            }
            // 2. 组装积分入账流水对象
            PointsMP pointsMP = new PointsMP()
                    .setOrderId(orderId)
                    .setUserId(userId)
                    .setPoints(pointValue)
                    .setType(1)   // 1-订单获得
                    .setStatus(1) // 1-已入账
                    .setDescription("订单消费自动赠送积分");

            // 3. 积分流水落库
           pointsService.save(pointsMP);
            System.out.println("【积分服务】用户 " + userId + " 成功增加积分 " + pointValue + " 点，流水入库完成！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    name = OrderConstants.POINTS_RELEASE_QUEUE,
                    durable = "true",
                    arguments = @Argument(name = "x-queue-mode", value = "lazy")
            ),
            exchange = @Exchange(
                    name = OrderConstants.ORDER_DIRECT_EXCHANGE,
                    type = "direct"
            ),
            key = OrderConstants.ORDER_PAY_SUCCESS_ROUTING_KEY

    ))
    public void onPointsReleaseMessage2(Map<String, Object> msg){
        try {
            Long orderId = Long.valueOf(msg.get("orderId").toString());
            Long userId = Long.valueOf(msg.get("userId").toString());
            Integer pointValue = Integer.valueOf(msg.get("pointValue").toString());

            // 如果积分小于等于0，不需要发放
            if (pointValue <= 0) {
                System.out.println("【积分服务】实付金额不足，赠送动积分为 0，跳过入库。");
                return;
            }
            // 2. 组装积分入账流水对象
            PointsMP pointsMP = new PointsMP()
                    .setOrderId(orderId)
                    .setUserId(userId)
                    .setPoints(pointValue)
                    .setType(1)   // 1-订单获得
                    .setStatus(1) // 1-已入账
                    .setDescription("订单消费自动赠送积分");

            // 3. 积分流水落库
            pointsService.save(pointsMP);
            System.out.println("【积分服务】用户 " + userId + " 成功增加积分 " + pointValue + " 点，流水入库完成！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(
//                    name = "aaa.queue",
//                    durable = "true"
//            ),
//            exchange = @Exchange(
//                    name = "aaa.exchange",
//                    type = "direct"
//            ),
//            key = OrderConstants.ORDER_PAY_SUCCESS_ROUTING_KEY
//    ))
//    public void onPointsReleaseMessage3(Map<String, Object> msg){
//    }
//
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(
//                    name = "fanout.queue",
//                    durable = "true"
//            ),
//            exchange = @Exchange(
//                    name = "fanout.exchange",
//                    type = "fanout"
//            )
//    ))
//    public void onPointsReleaseMessage4(Map<String, Object> msg){
//    }
//
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(
//                    name = "topic.queue",
//                    durable = "true"
//            ),
//            exchange = @Exchange(
//                    name = "topic.exchange",
//                    type = "topic"
//            ),
//            key = "topic.#"
//    ))
//    public void onPointsReleaseMessage5(Map<String, Object> msg){
//    }
//
}
