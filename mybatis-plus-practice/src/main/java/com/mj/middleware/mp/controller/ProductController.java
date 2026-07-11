package com.mj.middleware.mp.controller;

import com.mj.middleware.common.result.Result;
import com.mj.middleware.mp.entity.ProductMP;
import com.mj.middleware.mp.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品接口 — 演示条件构造器、乐观锁、聚合查询
 */
@Tag(name = "商品管理", description = "条件构造器、乐观锁、聚合查询演示")
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    // ==================== CRUD ====================

    @Operation(summary = "新增商品")
    @PostMapping
    public Result<Void> add(@RequestBody ProductMP product) {
        productService.save(product);
        return Result.success();
    }

    @Operation(summary = "根据 ID 查询")
    @GetMapping("/{id}")
    public Result<ProductMP> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @Operation(summary = "查询全部")
    @GetMapping("/list")
    public Result<List<ProductMP>> list() {
        return Result.success(productService.list());
    }

    // ==================== 条件查询 ====================

    @Operation(summary = "按分类和价格范围查询")
    @GetMapping("/by-condition")
    public Result<List<ProductMP>> byCondition(
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "最低价") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "最高价") @RequestParam(required = false) BigDecimal maxPrice) {
        return Result.success(productService.queryByCondition(category, minPrice, maxPrice));
    }

    @Operation(summary = "复杂查询（多条件组合）")
    @GetMapping("/complex")
    public Result<List<ProductMP>> complexQuery(
            @Parameter(description = "关键词（模糊匹配名称/描述）") @RequestParam(required = false) String keyword,
            @Parameter(description = "品牌") @RequestParam(required = false) String brand,
            @Parameter(description = "最低价") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "最低库存") @RequestParam(required = false) Integer minStock) {
        return Result.success(productService.complexQuery(keyword, brand, minPrice, minStock));
    }

    // ==================== 聚合统计 ====================

    @Operation(summary = "按品牌统计商品数量")
    @GetMapping("/count-by-brand")
    public Result<List<Map<String, Object>>> countByBrand() {
        return Result.success(productService.countByBrand());
    }

    // ==================== 更新操作 ====================

    @Operation(summary = "乐观锁更新价格")
    @PutMapping("/optimistic-lock")
    public Result<Boolean> updateWithOptimisticLock(
            @Parameter(description = "商品 ID") @RequestParam Long id,
            @Parameter(description = "新价格") @RequestParam BigDecimal newPrice) {
        return Result.success(productService.updateWithOptimisticLock(id, newPrice));
    }

    @Operation(summary = "批量更新价格（按分类涨价）")
    @PutMapping("/batch-price")
    public Result<Boolean> batchUpdatePrice(
            @Parameter(description = "分类") @RequestParam String category,
            @Parameter(description = "涨价金额") @RequestParam BigDecimal increaseAmount) {
        return Result.success(productService.batchUpdatePrice(category, increaseAmount));
    }
}
