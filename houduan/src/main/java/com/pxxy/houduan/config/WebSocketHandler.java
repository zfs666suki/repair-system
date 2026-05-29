package com.pxxy.houduan.config;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.swing.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器，用于管理实时通信连接
 * 支持基于用户ID的连接管理和消息推送
 */
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    /**
     * 存储在线用户的WebSocket会话，key为用户ID，value为对应的WebSocket会话
     * 使用ConcurrentHashMap，它由线程安全的哈希表实现，用于存储在线用户的WebSocket会话。
     * 它支持高并发访问，允许多线程同时进行读写操作而无需额外同步，确保在多线程环境下管理用户连接的数据安全性和性能。
     */
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    //- WebSocketSession 是什么？它是 Spring 对一条 WebSocket 连接的 封装对象 。
    //里面有网络连接信息、可以发送消息的方法等。你可以把它理解成「一根通往客户端的水管」，给这根水管喂数据，数据就流到了客户端。
    //- 为什么用 ConcurrentHashMap ？WebSocket 是多线程环境（每个连接一个线程），
    // ConcurrentHashMap 是线程安全的，多个人同时读写不会出问题。如果用普通 HashMap ，并发写入可能导致数据错乱或崩溃。
    //- 为什么用 Map<Long, WebSocketSession> ？ 因为我们需要根据用户 ID 找到他的连接。
    // 用户张三（ID=4）登录后，他的会话存为 {4: 张三的WebSocketSession} 。后续要给张三发消息时， sessions.get(4) 就能直接拿到他的连接。
    //- 这个 Map 存储在 内存 中（不是数据库），所以后端重启后所有连接信息都会丢失。但在线状态本身就是瞬态的，不需要持久化

    /**
     * 当WebSocket连接建立后的回调方法
     * 从连接路径中提取用户ID并保存会话信息
     * @param session 新建立的WebSocket会话对象
     */
    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) {
        //这里的session是WebSocketSession对象，它是Spring WebSocket框架创建的，用于表示WebSocket连接。
        //session对象 包含连接的所有信息，重要属性有 URI路径，用户ID，消息内容等等。
        //session对象 的方法有 获取属性方法外，还有 发送消息的方法 session.sendMessage(message)，接收消息的方法 session.receive()。
        String path = session.getUri() != null ? session.getUri().getPath() : null;
        //session.getUri() - 获取WebSocket连接的完整URI，比如 ws://localhost:8080/ws/1 - 协议：ws:// 主机：localhost 端口：8080 路径：/ws/12345
        //.getPath() - 获取URI路径，比如 /ws/1
        String userId = extractUserId(path);
        // "1"

        /*WebSocketSession 对象是由 Spring WebSocket 框架在底层自动创建和管理的：
        客户端发起连接请求
               ↓
        Tomcat/Undertow 容器接收请求
               ↓
        Spring WebSocket 框架拦截请求
               ↓
        框架自动创建 WebSocketSession 对象
               ↓
        你把 session 存入 sessions Map 中
        */
        if (userId != null) {
            try {
                //Long.parseLong(userId) 作用：将字符串类型的用户ID转换为长整型Long
                //sessions.put(...) 作用：将用户ID和对应的WebSocket会话存入ConcurrentHashMap
                //key：用户ID（Long类型） value：WebSocketSession对象（代表该用户的WebSocket连接）
                sessions.put(Long.parseLong(userId), session);
                logConnectionChange("建立", userId);
            } catch (NumberFormatException e) {
                logger.warn("WebSocket连接无效的用户ID格式: {}", userId);
            }
        } else {
            logger.warn("WebSocket连接缺少用户ID，路径: {}", path);
        }
    }

    /**
     * 当WebSocket连接关闭后的回调方法
     * 从会话映射中移除已断开的用户连接
     * @param session 即将关闭的WebSocket会话对象
     * @param status 连接关闭的状态信息
     */
    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session, @Nonnull CloseStatus status) {
        String path = session.getUri() != null ? session.getUri().getPath() : "";
        String userId = extractUserId(path);

        if (userId != null) {
            try {
                sessions.remove(Long.parseLong(userId));
                logConnectionChange("关闭", userId);
            } catch (NumberFormatException e) {
                logger.warn("WebSocket断开无效的用户ID格式: {}", userId);
            }
        }
    }

    /**
     * 当客户端发送文本消息时，Spring WebSocket 框架会自动调用这个方法
     * @param session 发送消息的WebSocket会话对象，代表当前发送消息的那个客户端连接
     * @param message 客户端发送的文本消息对象，封装了消息的所有信息
     */
    @Override
    protected void handleTextMessage(@Nonnull WebSocketSession session, @Nonnull TextMessage message) {
        /*
        `message.getPayload()` 用于从 WebSocket 文本消息对象中提取实际的消息内容。
         返回值：String 类型，即客户端发送的原始文本数据
                - 用途：获取消息正文，以便进行日志记录、解析或业务处理
                - 示例：若客户端发送 `"hello"`，则返回字符串 `"hello"`
        */
        logger.info("WebSocket收到消息: {}", message.getPayload());
    }

    /**
     * 向指定用户发送WebSocket消息
     * 如果用户不在线或会话已关闭，则记录警告日志
     * @param userId 目标用户的ID，表示要把消息发给谁
     * @param message 要发送的消息内容（文本格式）
     */
    public void sendMessage(Long userId, String message) {
        logger.info("=== WebSocket发送消息 ===");
        logger.info("目标用户ID: {}", userId);
        logger.info("当前在线用户数: {}", sessions.size());
        logger.info("在线用户列表: {}", sessions.keySet());
        
        WebSocketSession session = sessions.get(userId);
        // session.isOpen() 检测当前 WebSocketSession 连接是否已打开，返回 true 表示已打开，false 表示已关闭
        // session != null 检测当前用户是否在线
        if (session != null && session.isOpen()) {
            try {
                logger.info("用户 {} 在线，发送消息...", userId);
                // 步骤1: 将字符串包装成 WebSocket 协议要求的消息对象
                // TextMessage 是 WebSocketMessage 的子类，表示文本类型的 WebSocket 消息
                // new TextMessage(message) 将字符串包装成WebSocket的 TextMessage 对象 WebSocket协议要求消息必须是特定格式
                // 步骤2: 通过底层 WebSocket 连接发送数据
                session.sendMessage(new TextMessage(message));
                // session 的类型是 WebSocketSession（Spring WebSocket 框架提供的接口）
                // sendMessage() 是 Spring WebSocket 框架原生提供的方法，用于向已连接的客户端推送实时消息。
                // Spring 会调用底层容器（Tomcat/Undertow）的 WebSocket API
                // 将消息通过网络发送给客户端

                logger.info("WebSocket发送消息给用户 {} 成功: {}", userId, message);
            } catch (Exception e) {
                logger.error("WebSocket发送消息失败，用户ID: {}", userId, e);
            }
        } else {
            logger.warn("WebSocket用户 {} 未在线（会话: {}, 状态: {}），消息将通过系统消息发送", 
                userId, session, session != null ? session.isOpen() : "null");
        }
    }

    /**
     * 从WebSocket连接路径中提取用户ID
     * 假设路径格式为 /ws/{userId} 或类似形式
     * @param path WebSocket连接的URI路径
     * @return 提取到的用户ID，如果路径无效或格式不正确则返回null
     */
    private String extractUserId(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String[] parts = path.split("/");
        //   parts[0] = ""        // 第一个/之前的空字符串
        //   parts[1] = "ws"      // 第一个/和第二个/之间的内容
        //   parts[2] = "1"     // 最后一个/之后的内容
        //   ["", "ws", "1"]
        //    0    1     2
        if (parts.length >= 3) {
            return parts[parts.length - 1];
            // parts[3 - 1] = "1"
        }
        return null;
    }

    /**
     * 记录用户连接状态变化的日志信息
     * @param action 连接操作类型（如"建立"、"关闭"）
     * @param userId 操作用户的ID
     */
    private void logConnectionChange(String action, String userId) {
        logger.info("用户 {} {}WebSocket连接，当前连接数: {}", userId, action, sessions.size());
    }

    /**
     * 获取所有在线用户ID列表
     * @return 在线用户ID列表
     */
    public java.util.Set<Long> getOnlineUserIds() {
        return sessions.keySet();
    }

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isOnline(Long userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }

}
