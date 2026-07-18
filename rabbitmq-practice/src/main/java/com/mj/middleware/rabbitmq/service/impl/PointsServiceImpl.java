package com.mj.middleware.rabbitmq.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.middleware.rabbitmq.entity.PointsMP;
import com.mj.middleware.rabbitmq.mapper.PointsMapper;
import com.mj.middleware.rabbitmq.service.IPointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 积分 Service 实现
 */
@Slf4j
@Service
public class PointsServiceImpl extends ServiceImpl<PointsMapper, PointsMP> implements IPointsService {

    @Override
    public PointsMP get(Long id) {
        return getById(id);
    }

    @Override
    public void addPoints(PointsMP points) {
        boolean result = save(points);
        log.info("新增积分记录: userId={}, orderId={}, points={}, 结果: {}",
                points.getUserId(), points.getOrderId(), points.getPoints(), result);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        PointsMP exist = getById(id);
        if (exist == null) {
            throw new RuntimeException("积分记录不存在: id=" + id);
        }
        exist.setStatus(status);
        updateById(exist);
    }

    @Override
    public List<PointsMP> getByOrderId(Long orderId) {
        return lambdaQuery().eq(PointsMP::getOrderId, orderId).list();
    }

    @Override
    public List<PointsMP> getByUserId(Long userId) {
        return lambdaQuery().eq(PointsMP::getUserId, userId).list();
    }

    @Override
    public IPage<PointsMP> listPoints(Integer pageNum, Integer pageSize) {
        Page<PointsMP> page = new Page<>(pageNum, pageSize);
        return lambdaQuery().page(page);
    }
}
