package com.mj.middleware.rabbitmq.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mj.middleware.common.result.Result;
import com.mj.middleware.rabbitmq.entity.PointsMP;
import com.mj.middleware.rabbitmq.service.IPointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分接口 — 演示 RabbitMQ 订单场景下的积分发放
 */
@Tag(name = "积分管理", description = "积分 CRUD 接口")
@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointsController {

    private final IPointsService pointsService;

    @Operation(summary = "新增积分记录")
    @PostMapping
    public Result<Void> addPoints(@RequestBody PointsMP points) {
        pointsService.addPoints(points);
        return Result.success();
    }

    @Operation(summary = "根据 ID 查询积分记录")
    @GetMapping("/{id}")
    public Result<PointsMP> getPoints(@PathVariable Long id) {
        return Result.success(pointsService.get(id));
    }

    @Operation(summary = "更新积分状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        pointsService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "根据订单ID查询积分记录")
    @GetMapping("/order/{orderId}")
    public Result<List<PointsMP>> getByOrderId(@PathVariable Long orderId) {
        return Result.success(pointsService.getByOrderId(orderId));
    }

    @Operation(summary = "根据用户ID查询积分记录")
    @GetMapping("/user/{userId}")
    public Result<List<PointsMP>> getByUserId(@PathVariable Long userId) {
        return Result.success(pointsService.getByUserId(userId));
    }

    @Operation(summary = "分页查询积分记录")
    @GetMapping("/page")
    public Result<IPage<PointsMP>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(pointsService.listPoints(pageNum, pageSize));
    }
}
