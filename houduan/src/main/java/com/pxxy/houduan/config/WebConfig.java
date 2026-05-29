package com.pxxy.houduan.config;

import com.pxxy.houduan.common.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Web MVC配置类
 * 配置拦截器、跨域等Web相关功能
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtUtils jwtUtils;

    public WebConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 配置CORS跨域
     */
    @Bean
    public CorsFilter corsFilter() {
        // 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源
        config.addAllowedOriginPattern("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有HTTP方法
        config.addAllowedMethod("*");
        // 允许携带凭证
        config.setAllowCredentials(true);
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);
        // 暴露的响应头
        config.setExposedHeaders(Arrays.asList("Authorization"));
        // 创建CORS过滤器
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 注册CORS配置
        source.registerCorsConfiguration("/**", config);
        // 返回CORS过滤器
        return new CorsFilter(source);
    }

    /**
     * 注册JWT认证拦截器
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 配置JWT拦截器的拦截路径和排除路径
        registry.addInterceptor(new JwtInterceptor(jwtUtils))
                .addPathPatterns("/api/**")// 拦截所有/api/**路径
                .excludePathPatterns // 排除路径
                        (
                        "/api/user/login",// 登录接口
                        "/doc.html",//文档接口
                        "/webjars/**", //排除Swagger UI路径
                        "/v3/api-docs/**",// 排除OpenAPI 文档路径
                        "/ws/**",           // 排除WebSocket路径
                        "/sockjs/**"        // 排除SockJS路径
                );
    }
/*
InterceptorRegistry 来自 Spring Web Servlet 模块，用于注册和管理拦截器。
  常用方法：
    addInterceptor(HandlerInterceptor interceptor)：注册一个拦截器，返回 InterceptorRegistration 对象
    getRegistrations()：获取所有已注册的拦截器配置
  InterceptorRegistration 对象的常用方法（链式调用）：
    addPathPatterns(String... patterns)：设置需要拦截的路径模式，如 /api/**
    excludePathPatterns(String... patterns)：设置排除的路径模式，如登录接口、静态资源
    order(int order)：设置拦截器的执行顺序，数值越小优先级越高
 实际使用：在此代码中，先通过 addInterceptor() 注册 JwtInterceptor，然后配置拦截所有 /api/** 路径，但排除登录接口和 API 文档相关路径
 */
}
