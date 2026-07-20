package com.mj.middleware.nacos.consumer.feign.client;

import com.mj.middleware.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 调用 nacos-provider 服务的 Feign 客户端
 * name = provider 在 Nacos 注册的服务名
 * LoadBalancer 自动实现负载均衡（轮询/随机）
 */
@FeignClient(name = "nacos-provider", contextId = "userFeignClient", path = "/user")
public interface UserFeignClient {

    @PostMapping
    Result<Map<String, Object>> create(@RequestBody Map<String, Object> user);

    @GetMapping("/{id}")
    Result<Map<String, Object>> getById(@PathVariable("id") Long id);

    @GetMapping("/list")
    Result<List<Map<String, Object>>> list();

    @PutMapping
    Result<Boolean> update(@RequestBody Map<String, Object> user);

    @DeleteMapping("/{id}")
    Result<Boolean> delete(@PathVariable("id") Long id);

    @GetMapping("/config")
    Result<Map<String, Object>> config();

    @GetMapping("/echo/{msg}")
    Result<Map<String, String>> echo(@PathVariable("msg") String msg);
}
