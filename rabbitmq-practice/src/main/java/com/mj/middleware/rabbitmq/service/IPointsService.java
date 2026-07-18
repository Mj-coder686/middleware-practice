package com.mj.middleware.rabbitmq.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.middleware.rabbitmq.entity.PointsMP;

import java.util.List;

/**
 * 积分 Service 接口 — 继承 IService 拥有批量 CRUD
 */
public interface IPointsService extends IService<PointsMP> {

    /** 根据 ID 查询积分记录 */
    PointsMP get(Long id);

    /** 新增积分记录 */
    void addPoints(PointsMP points);

    /** 更新积分状态 */
    void updateStatus(Long id, Integer status);

    /** 根据订单ID查询积分记录 */
    List<PointsMP> getByOrderId(Long orderId);

    /** 根据用户ID查询积分记录 */
    List<PointsMP> getByUserId(Long userId);

    /** 分页查询积分记录 */
    IPage<PointsMP> listPoints(Integer pageNum, Integer pageSize);
}
