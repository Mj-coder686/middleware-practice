package com.mj.middleware.nacos.consumer.controller;

import com.mj.middleware.common.result.Result;
import com.mj.middleware.nacos.consumer.config.NacosConfig;
import com.mj.middleware.nacos.consumer.feign.client.ProductClient;
import com.mj.middleware.nacos.consumer.feign.client.UserFeignClient;
import com.mj.middleware.nacos.consumer.feign.po.TProduct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费者 Controller — 通过 Feign 远程调用 Provider 的 CRUD 接口
 * 用于验证：配置注入、Feign 调用、负载均衡
 */
@Tag(name = "消费者接口", description = "Feign 远程调用 + Nacos 配置验证")
@RestController
@RequestMapping("/consumer")
@RequiredArgsConstructor
public class ConsumerController {

    private final UserFeignClient userFeignClient;
    private final ProductClient productClient;
    private final NacosConfig nacosConfig;

    // ==================== Feign 远程 CRUD ====================

    @Operation(summary = "远程新增用户")
    @PostMapping("/user")
    public Result<Map<String, Object>> createUser(@RequestBody Map<String, Object> user) {
        return userFeignClient.create(user);
    }

    @Operation(summary = "远程查询用户")
    @GetMapping("/user/{id}")
    public Result<Map<String, Object>> getUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return userFeignClient.getById(id);
    }

    @Operation(summary = "远程查询全部用户")
    @GetMapping("/user/list")
    public Result<List<Map<String, Object>>> listUsers() {
        return userFeignClient.list();
    }

    @Operation(summary = "远程修改用户")
    @PutMapping("/user")
    public Result<Boolean> updateUser(@RequestBody Map<String, Object> user) {
        return userFeignClient.update(user);
    }

    @Operation(summary = "远程删除用户")
    @DeleteMapping("/user/{id}")
    public Result<Boolean> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return userFeignClient.delete(id);
    }

    // ==================== Nacos 配置验证 ====================

    @Operation(summary = "查看 Consumer 自身的 Nacos 配置")
    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        Map<String, Object> info = new HashMap<>();
        info.put("appName", nacosConfig.getAppName());
        info.put("env", nacosConfig.getEnv());
        info.put("featureEnabled", nacosConfig.getFeatureEnabled());
        info.put("welcomeMessage", nacosConfig.getWelcomeMessage());
        info.put("loadBalancer", nacosConfig.getLoadBalancer());
        return Result.success(info);
    }

    @Operation(summary = "查看 Provider 端的 Nacos 配置（通过 Feign 调用）")
    @GetMapping("/provider-config")
    public Result<Map<String, Object>> providerConfig() {
        return userFeignClient.config();
    }

    // ==================== 负载均衡验证 ====================

    @Operation(summary = "回显 — 验证 Feign 负载均衡分发到哪个 Provider 实例")
    @GetMapping("/echo/{msg}")
    public Result<Map<String, String>> echo(
            @Parameter(description = "回显消息") @PathVariable String msg) {
        return userFeignClient.echo(msg);
    }

    @GetMapping("product")
    public List<TProduct> getProductList() {
        return productClient.listAll();
    }
}
