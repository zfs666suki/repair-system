package com.pxxy.houduan.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * 配置分页插件等MyBatis-Plus相关功能
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 配置MyBatis-Plus拦截器
     * 添加MySQL分页功能支持
     *
     * @return MybatisPlusInterceptor拦截器对象
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建mybatisPlus拦截器对象
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //addInnerInterceptor()注册各种功能插件到拦截器链中可添加分页、数据权限、乐观锁等拦截器
        //PaginationInnerInterceptor 是MyBatis-Plus的分页拦截器
        //DbType.MYSQL 指定数据库类型为MySQ
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
