package com.mj.middleware.mp.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 核心配置
 *
 * 1. 分页插件      — PaginationInnerInterceptor
 * 2. 乐观锁插件    — OptimisticLockerInnerInterceptor
 * 3. 防全表更新插件 — BlockAttackInnerInterceptor
 * 4. 自动填充      — MetaObjectHandler
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 注册 MybatisPlusInterceptor（插件按 add 顺序执行）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件（必须设置 DbType）
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.H2);
        paginationInterceptor.setMaxLimit(500L);  // 单页最大条数限制
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 2. 乐观锁插件（必须在分页之前，但实际建议放在分页之后）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 3. 防全表更新/删除插件（防止不带 WHERE 条件的全表操作）
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 自动填充处理器
     *
     * 配合实体类 @TableField(fill = FieldFill.INSERT) 注解使用
     * 当执行 insert 操作时自动填充 createTime
     * 当执行 update 操作时自动填充 updateTime
     */
    @Component
    public static class AutoFillHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            // strictInsertFill 有值时不覆盖；fillStrategy 无论是否有值都填充
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
    }
}
