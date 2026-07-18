package com.mj.middleware.rabbitmq.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mj.middleware.common.result.Result;
import com.mj.middleware.rabbitmq.entity.OrderMP;
import com.mj.middleware.rabbitmq.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单接口 — 演示 RabbitMQ 订单场景下的 CRUD
 */
@Tag(name = "订单管理", description = "订单 CRUD 接口")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<Void> createOrder(@RequestBody OrderMP order) {
        orderService.createOrder(order);
        return Result.success();
    }

    @Operation(summary = "根据 ID 查询订单")
    @GetMapping("/{id}")
    public Result<OrderMP> getOrder(@PathVariable Long id) {
        return Result.success(orderService.get(id));
    }

    @Operation(summary = "更新订单")
    @PutMapping("/{id}")
    public Result<Void> updateOrder(@PathVariable Long id, @RequestBody OrderMP order) {
        orderService.updateOrder(id, order);
        return Result.success();
    }

    @Operation(summary = "删除订单（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return Result.success();
    }

    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public Result<IPage<OrderMP>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.listOrder(pageNum, pageSize));
    }
}
