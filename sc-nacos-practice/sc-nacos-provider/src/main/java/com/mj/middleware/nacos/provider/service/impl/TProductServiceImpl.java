package com.mj.middleware.nacos.provider.service.impl;

import com.mj.middleware.nacos.provider.domain.po.TProduct;
import com.mj.middleware.nacos.provider.mapper.TProductMapper;
import com.mj.middleware.nacos.provider.service.ITProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author MJ
 * @since 2026-07-20
 */
@Service
public class TProductServiceImpl extends ServiceImpl<TProductMapper, TProduct> implements ITProductService {

}
