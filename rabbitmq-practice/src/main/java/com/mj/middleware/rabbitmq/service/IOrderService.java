package com.mj.middleware.rabbitmq.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.middleware.rabbitmq.entity.OrderMP;

/**
 * 订单 Service 接口 — 继承 IService 拥有批量 CRUD
 */
public interface IOrderService extends IService<OrderMP> {

    /** 根据 ID 查询订单 */
    OrderMP get(Long id);

    /** 创建订单 */
    void createOrder(OrderMP order);

    /** 更新订单（只更新非 null 字段） */
    void updateOrder(Long id, OrderMP order);

    /** 删除订单（逻辑删除） */
    void deleteOrder(Long id);

    /** 分页查询订单 */
    IPage<OrderMP> listOrder(Integer pageNum, Integer pageSize);
}
