package com.mj.middleware.nacos.provider.controller;


import com.mj.middleware.nacos.provider.domain.po.TProduct;
import com.mj.middleware.nacos.provider.service.ITProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author MJ
 * @since 2026-07-20
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/t-product")
public class TProductController {

    @Autowired
    private ITProductService tProductService;

    // ==================== 1. 基础查询接口 ====================

    /**
     * 根据ID查询单个产品详情
     */
    @GetMapping("/{id}")
    public TProduct getById(@PathVariable("id") Long id) {
        return tProductService.getById(id);
    }

    /**
     * 根据多个ID进行批量查询
     */
    @GetMapping("/list-by-ids")
    public List<TProduct> selectListByIds(@RequestParam("ids") List<Long> ids) {
        return tProductService.listByIds(ids);
    }

    /**
     * 查询所有产品列表（不分页）
     */
    @GetMapping("/list-all")
    public List<TProduct> listAll() {
        return tProductService.list();
    }

    /**
     * 获取满足条件的总记录数（演示 MP 计数）
     */
    @GetMapping("/count")
    public Long countProducts(@RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<TProduct> queryWrapper = new LambdaQueryWrapper<>();
        // 如果传了状态就按状态查，没传就查全部
        queryWrapper.eq(status != null, TProduct::getStatus, status);
        return tProductService.count(queryWrapper);
    }

    // ==================== 2. 复杂条件与分页 ====================

    /**
     * 条件多字段模糊查询 + 分页
     * 演示直接用 RequestParam 接收常用分页参数，省去建 DTO 的麻烦
     */
    @GetMapping("/page")
    public Page<TProduct> queryProductPage(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category) {

        // 1. 创建分页对象
        Page<TProduct> page = new Page<>(pageNo, pageSize);

        // 2. 构建 MP 的 Lambda 条件构造器
        LambdaQueryWrapper<TProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(name != null && !name.isEmpty(), TProduct::getName, name)       // 名字模糊匹配
                .eq(category != null && !category.isEmpty(), TProduct::getCategory, category) // 分类精准匹配
                .orderByDesc(TProduct::getCreateTime);                              // 默认按创建时间倒序

        // 3. 执行分页查询
        return tProductService.page(page, queryWrapper);
    }

    // ==================== 3. 增 / 删 / 改 接口 ====================

    /**
     * 新增单个产品
     */
    @PostMapping
    public Boolean saveProduct(@RequestBody TProduct tProduct) {
        return tProductService.save(tProduct);
    }

    /**
     * 批量新增产品（演示 MP 批量入库功能）
     */
    @PostMapping("/batch")
    public Boolean saveProductBatch(@RequestBody List<TProduct> productList) {
        return tProductService.saveBatch(productList);
    }

    /**
     * 根据主键 ID 修改产品信息
     */
    @PutMapping
    public Boolean updateProduct(@RequestBody TProduct tProduct) {
        return tProductService.updateById(tProduct);
    }

    /**
     * 根据 ID 删除单个产品
     */
    @DeleteMapping("/{id}")
    public Boolean deleteById(@PathVariable("id") Long id) {
        return tProductService.removeById(id);
    }

    /**
     * 批量删除产品
     */
    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestParam("ids") List<Long> ids) {
        return tProductService.removeByIds(ids);
    }
}
