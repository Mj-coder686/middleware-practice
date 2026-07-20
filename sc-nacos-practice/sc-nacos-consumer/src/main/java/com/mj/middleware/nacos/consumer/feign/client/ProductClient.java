package com.mj.middleware.nacos.consumer.feign.client;


import com.mj.middleware.nacos.consumer.feign.po.TProduct;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "nacos-provider", contextId = "productClient", path = "/t-product")
public interface ProductClient {

    @GetMapping("/{id}")
    TProduct getById(@PathVariable("id") Long id);

    @GetMapping("/list-by-ids")
    List<TProduct> selectListByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/list-all")
    List<TProduct> listAll();
}
