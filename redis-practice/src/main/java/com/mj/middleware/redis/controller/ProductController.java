package com.mj.middleware.redis.controller;

import com.mj.middleware.common.result.Result;
import com.mj.middleware.redis.entity.Product;
import com.mj.middleware.redis.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品接口 — 发布 / 收藏 / Feed 推送
 */
@Tag(name = "商品管理", description = "商品发布 & 收藏 & Feed推送")
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @Operation(summary = "发布商品（自动推送到粉丝 Feed）")
    @PostMapping
    public Result<Long> publishProduct(@RequestBody Product product) {
        return Result.success(productService.publish(product));
    }

    @Operation(summary = "查询商品详情")
    @GetMapping("/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @Operation(summary = "上架商品")
    @PutMapping("/{id}/on-sale")
    public Result<Void> putOnSale(@PathVariable Long id) {
        productService.putOnSale(id);
        return Result.success();
    }

    @Operation(summary = "下架商品")
    @PutMapping("/{id}/off-sale")
    public Result<Void> pullOffSale(@PathVariable Long id) {
        productService.pullOffSale(id);
        return Result.success();
    }

    @Operation(summary = "收藏商品")
    @PostMapping("/{id}/like/{userId}")
    public Result<Boolean> likeProduct(@PathVariable Long id, @PathVariable Long userId) {
        return Result.success(productService.like(id, userId));
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{id}/like/{userId}")
    public Result<Boolean> unlikeProduct(@PathVariable Long id, @PathVariable Long userId) {
        return Result.success(productService.unlike(id, userId));
    }

    @Operation(summary = "查询商家的商品列表")
    @GetMapping("/merchant/{merchantId}")
    public Result<List<Product>> getByMerchant(
            @PathVariable Long merchantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(productService.getByMerchant(merchantId, pageNum, pageSize));
    }

    @Operation(summary = "按分类查询商品（按销量排序）")
    @GetMapping("/category/{category}")
    public Result<List<Product>> getByCategory(
            @PathVariable String category,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(productService.getByCategory(category, pageNum, pageSize));
    }
}
