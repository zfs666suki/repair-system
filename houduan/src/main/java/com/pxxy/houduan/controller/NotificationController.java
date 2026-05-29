package com.pxxy.houduan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pxxy.houduan.common.JwtUtils;
import com.pxxy.houduan.common.PageResult;
import com.pxxy.houduan.common.Result;
import com.pxxy.houduan.entity.Notification;
import com.pxxy.houduan.entity.SysUser;
import com.pxxy.houduan.mapper.SysUserMapper;
import com.pxxy.houduan.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知控制器
 * 提供消息查询、标记已读、发送聊天消息等功能的接口
 */
@RestController
@RequestMapping("/api/notification")
@Tag(name = "消息通知管理", description = "消息通知相关接口")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;
    private final JwtUtils jwtUtils;
    private final SysUserMapper sysUserMapper;

    public NotificationController(NotificationService notificationService, JwtUtils jwtUtils, SysUserMapper sysUserMapper) {
        this.notificationService = notificationService;
        this.jwtUtils = jwtUtils;
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 查询当前用户的所有消息列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 消息通知列表
     */
    @Operation(summary = "查询消息列表", description = "获取当前用户的消息通知列表，支持分页")
    @GetMapping("/list")
    public PageResult<?> listNotifications(
            @Parameter(description = "页码，默认1", required = false) @RequestParam(defaultValue = "1") long pageNum,
            @Parameter(description = "每页数量，默认10", required = false) @RequestParam(defaultValue = "10") long pageSize,
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true) @RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.getUserIdFromTokenWithBearer(token);
        IPage<Notification> page = notificationService.getUserNotificationsPage(userId, pageNum, pageSize);
        
        // 关联查询用户表，添加发送者姓名
        List<Map<String, Object>> records = new ArrayList<>();
        for (Notification notification : page.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", notification.getId());
            item.put("userId", notification.getUserId());
            item.put("type", notification.getType());
            item.put("subType", notification.getSubType());
            item.put("title", notification.getTitle());
            item.put("content", notification.getContent());
            item.put("orderId", notification.getOrderId());
            item.put("relatedUserId", notification.getRelatedUserId());
            item.put("isRead", notification.getIsRead());
            item.put("createTime", notification.getCreateTime());
            
            // 查询发送者姓名（relatedUserId是发送者ID）
            Long senderId = notification.getRelatedUserId();
            String senderName = "未知用户";
            if (senderId != null) {
                SysUser sender = sysUserMapper.selectById(senderId);
                if (sender != null) {
                    senderName = sender.getRealName() != null && !sender.getRealName().isEmpty() 
                        ? sender.getRealName() 
                        : sender.getUsername() != null ? sender.getUsername() : "未知用户";
                }
            }
            item.put("senderName", senderName);
            
            // 查询接收者姓名（userId是接收者ID）
            Long receiverId = notification.getUserId();
            String receiverName = "未知用户";
            if (receiverId != null) {
                SysUser receiver = sysUserMapper.selectById(receiverId);
                if (receiver != null) {
                    receiverName = receiver.getRealName() != null && !receiver.getRealName().isEmpty() 
                        ? receiver.getRealName() 
                        : receiver.getUsername() != null ? receiver.getUsername() : "未知用户";
                }
            }
            item.put("receiverName", receiverName);
            
            // 计算聊天对象姓名（用于消息列表显示）
            Long currentUserId = userId;
            String chatUserName = "未知用户";
            Integer chatUserRole = null;
            // 判断消息是发送还是接收
            Long relatedUserId = notification.getRelatedUserId();
            if (relatedUserId != null && relatedUserId.equals(currentUserId)) {
                // 自己发送的消息，聊天对象是接收者
                chatUserName = receiverName;
                chatUserRole = receiverId != null ? sysUserMapper.selectById(receiverId).getRole() : null;
            } else {
                // 接收的消息，聊天对象是发送者
                chatUserName = senderName;
                chatUserRole = relatedUserId != null ? sysUserMapper.selectById(relatedUserId).getRole() : null;
            }
            item.put("chatUserName", chatUserName);
            item.put("chatUserRole", chatUserRole);
            
            records.add(item);
        }
        
        logger.info("用户 {} 查询消息列表: pageNum={}, pageSize={}, total={}", userId, pageNum, pageSize, page.getTotal());
        return PageResult.success(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 获取未读消息数量
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 未读消息数量
     */
    @Operation(summary = "获取未读消息数量", description = "获取当前用户的未读消息数量")
    @GetMapping("/unread-count")
    public Result<?> getUnreadCount(
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true)
            @RequestHeader("Authorization") String token) {
        // 从令牌中解析用户ID并统计未读数
        Long userId = jwtUtils.getUserIdFromTokenWithBearer(token);
        int count = notificationService.getUnreadCount(userId);
        logger.info("用户 {} 未读消息数量: {}", userId, count);
        return Result.success(count);
    }

    /**
     * 获取指定类型的未读消息数量
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @param type 消息类型：1-系统通知，2-个人消息
     * @return 未读消息数量
     */
    @Operation(summary = "获取指定类型未读消息数量", description = "获取当前用户指定类型的未读消息数量")
    @GetMapping("/unread-count/type")
    public Result<?> getUnreadCountByType(
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "消息类型：1-系统通知，2-个人消息", required = true)
            @RequestParam Integer type) {
        Long userId = jwtUtils.getUserIdFromTokenWithBearer(token);
        int count = notificationService.getUnreadCountByType(userId, type);
        logger.info("用户 {} 类型 {} 的未读消息数量: {}", userId, type, count);
        return Result.success(count);
    }

    /**
     * 标记指定消息为已读状态
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @param id 消息ID
     * @return 操作结果提示
     */
    @Operation(summary = "标记消息已读", description = "标记指定消息为已读状态")
    @PutMapping("/{id}/read")
    public Result<?> markAsRead(
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "消息ID", required = true) @PathVariable Long id) {
        // 从令牌中解析用户ID并执行标记操作
        Long userId = jwtUtils.getUserIdFromTokenWithBearer(token);
        notificationService.markAsRead(id, userId);
        logger.info("用户 {} 标记消息已读: {}", userId, id);
        return Result.success("标记已读成功");
    }

    /**
     * 标记与指定用户的所有聊天消息为已读
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @param otherUserId 对方用户ID
     * @return 操作结果提示
     */
    @Operation(summary = "批量标记聊天消息已读", description = "标记与指定用户的所有未读聊天消息为已读")
    @PutMapping("/chat/read")
    public Result<?> markChatMessagesAsRead(
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "对方用户ID", required = true) @RequestParam Long otherUserId) {
        Long userId = jwtUtils.getUserIdFromTokenWithBearer(token);
        notificationService.markAllAsRead(userId, otherUserId);
        logger.info("用户 {} 标记与用户 {} 的所有聊天消息已读", userId, otherUserId);
        return Result.success("标记已读成功");
    }

    /**
     * 发送个人聊天消息给其他用户
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @param request 聊天请求对象，包含接收者ID、标题和内容
     * @return 操作结果提示
     */
    @Operation(summary = "发送个人消息", description = "发送个人聊天消息给其他用户")
    @PostMapping("/chat")
    public Result<?> sendChatMessage(
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "聊天请求对象，包含receiverId（接收者ID）、title（标题）、content（内容）", required = true) @RequestBody ChatRequest request) {
        // 从令牌中解析发送者ID并发送消息
        Long senderId = jwtUtils.getUserIdFromToken(token.substring(7));
        notificationService.sendPersonalNotification(senderId, request.getReceiverId(), 3, request.getTitle(), request.getContent());
        logger.info("用户 {} 发送消息给用户 {}: {}", senderId, request.getReceiverId(), request.getTitle());
        return Result.success("发送消息成功");
    }

    /**
     * 聊天请求数据传输对象
     * 封装发送个人消息所需的参数
     */
    @Data
    public static class ChatRequest {
        private Long receiverId;
        private String title;
        private String content;
    }

    /**
     * 获取与指定用户的聊天记录
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @param otherUserId 对方用户ID
     * @return 双方之间的聊天消息列表
     */
    @Operation(summary = "获取聊天记录", description = "获取与指定用户的双向聊天记录")
    @GetMapping("/chat")
    public Result<?> getChatMessages(
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "对方用户ID", required = true) @RequestParam Long otherUserId) {
        // 从令牌中解析当前用户ID并查询双向聊天记录
        Long userId = jwtUtils.getUserIdFromTokenWithBearer(token);
        List<Notification> messages = notificationService.getChatMessages(userId, otherUserId);
        
        // 关联查询用户表，添加发送者姓名
        List<Map<String, Object>> result = new ArrayList<>();
        for (Notification message : messages) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", message.getId());
            item.put("userId", message.getUserId());
            item.put("type", message.getType());
            item.put("subType", message.getSubType());
            item.put("title", message.getTitle());
            item.put("content", message.getContent());
            item.put("orderId", message.getOrderId());
            item.put("relatedUserId", message.getRelatedUserId());
            item.put("isRead", message.getIsRead());
            item.put("createTime", message.getCreateTime());
            
            // 查询发送者姓名（relatedUserId是发送者ID）
            Long senderId = message.getRelatedUserId();
            String senderName = "未知用户";
            if (senderId != null) {
                SysUser sender = sysUserMapper.selectById(senderId);
                if (sender != null) {
                    senderName = sender.getRealName() != null && !sender.getRealName().isEmpty() 
                        ? sender.getRealName() 
                        : sender.getUsername() != null ? sender.getUsername() : "未知用户";
                }
            }
            item.put("senderName", senderName);
            
            result.add(item);
        }
        
        logger.info("用户 {} 查看与用户 {} 的聊天记录，共 {} 条", userId, otherUserId, messages.size());
        return Result.success(result);
    }
}
