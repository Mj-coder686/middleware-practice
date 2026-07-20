package com.mj.middleware.nacos.provider.controller;

import com.mj.middleware.common.result.Result;
import com.mj.middleware.nacos.provider.config.NacosConfig;
import com.mj.middleware.nacos.provider.entity.UserEntity;
import com.mj.middleware.nacos.provider.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理 Controller — 供 Consumer 通过 Feign 调用
 */
@Tag(name = "用户管理", description = "User CRUD 接口（Provider 端）")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final NacosConfig nacosConfig;

    @Value("${server.port}")
    private String serverPort;

    // ==================== CRUD ====================

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<UserEntity> create(@RequestBody UserEntity user) {
        userService.save(user);
        return Result.success(user);
    }

    @Operation(summary = "根据 ID 查询用户")
    @GetMapping("/{id}")
    public Result<UserEntity> getById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @Operation(summary = "查询全部用户")
    @GetMapping("/list")
    public Result<List<UserEntity>> list() {
        return Result.success(userService.list());
    }

    @Operation(summary = "修改用户")
    @PutMapping
    public Result<Boolean> update(@RequestBody UserEntity user) {
        return Result.success(userService.updateById(user));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    // ==================== 配置 & 负载均衡验证 ====================

    @Operation(summary = "查看 Nacos 注入的配置信息")
    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        Map<String, Object> info = new HashMap<>();
        info.put("serverPort", serverPort);
        info.put("appName", nacosConfig.getAppName());
        info.put("env", nacosConfig.getEnv());
        info.put("dbDesc", nacosConfig.getDbDesc());
        info.put("redisDesc", nacosConfig.getRedisDesc());
        return Result.success(info);
    }

    @Operation(summary = "回显接口 — 用于验证负载均衡分发到了哪个实例")
    @GetMapping("/echo/{msg}")
    public Result<Map<String, String>> echo(
            @Parameter(description = "回显消息") @PathVariable String msg) {
        Map<String, String> result = new HashMap<>();
        result.put("msg", msg);
        result.put("serverPort", serverPort);
        return Result.success(result);
    }
}
