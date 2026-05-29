package com.pxxy.houduan.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 启用WebSocket支持并注册WebSocket处理器
 */
@Configuration
@EnableWebSocket //用于启用 WebSocket 支持。
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebSocketHandler webSocketHandler;

    /**
     * 注册WebSocket处理器和路径映射
     * @param registry WebSocket处理器注册表
     */
    @Override
    // WebSocketConfigurer这个接口只有一个方法registerWebSocketHandlers
    // 这个方法的作用是告诉Spring容器，当有新的WebSocket连接时，应该使用哪个处理器来处理该连接。
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //当客户端连接到 ws://localhost:8080/ws/5 时，交给 webSocketHandler 处理。 {userId} 是路径变量
        registry.addHandler(webSocketHandler, "/ws/{userId}")
                .setAllowedOrigins("*");// 允许所有来源的连接允许跨域。
                // 前端在 localhost:5173 ，后端在 8080 ，不加这行浏览器会拒绝连接
    }
/*
WebSocketHandlerRegistry 来自 Spring WebSocket 模块，用于注册和管理 WebSocket 处理器。
  常用方法：
    addHandler(WebSocketHandler handler, String... paths)：注册 WebSocket 处理器并映射路径，返回 WebSocketHandlerRegistration 对象
    addHandler(WebSocketHandler handler, String path)：注册单个路径的处理器
  WebSocketHandlerRegistration 对象的常用方法（链式调用）：
    setAllowedOrigins(String... origins)：设置允许跨域的来源，"*" 表示允许所有来源
    addInterceptors(HandshakeInterceptor... interceptors)：添加握手拦截器，用于在建立连接前后执行逻辑
    withSockJS()：启用 SockJS 支持，用于兼容不支持 WebSocket 的浏览器
  实际使用：在此代码中，通过 addHandler() 注册了 webSocketHandler，映射到 /ws/{userId} 路径，并设置允许所有来源的跨域连接
*/
}
