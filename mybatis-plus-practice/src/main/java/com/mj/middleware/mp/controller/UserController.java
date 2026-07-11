package com.mj.middleware.mp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mj.middleware.common.result.Result;
import com.mj.middleware.mp.entity.UserMP;
import com.mj.middleware.mp.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户接口 — 演示 IService CRUD + 分页
 */
@Tag(name = "用户管理", description = "MyBatis-Plus IService CRUD 演示")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    // ==================== 基础 CRUD ====================

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> addUser(@RequestBody UserMP user) {
        userService.add(user);
        return Result.success();
    }

    @Operation(summary = "根据 ID 查询")
    @GetMapping("/{id}")
    public Result<UserMP> getUser(@PathVariable Long id) {
        return Result.success(userService.get(id));
    }

    @Operation(summary = "查询全部用户")
    @GetMapping("/list")
    public Result<List<UserMP>> list() {
        return Result.success(userService.list());
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody UserMP user) {
        userService.updateUser(id, user);
        return Result.success();
    }

    @Operation(summary = "删除用户（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        userService.removeBatchByIds(ids);
        return Result.success();
    }

    // ==================== 批量操作 ====================

    @Operation(summary = "批量插入演示")
    @PostMapping("/add/list")
    public Result<Void> addList(@RequestBody List<UserMP> userList) {
        userService.addList(userList);
        return Result.success();
    }

    // ==================== 分页查询 ====================

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<IPage<UserMP>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.listUser(pageNum, pageSize));
    }
}
