package com.pxxy.houduan.common;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 权限检查工具类
 * 提供统一的角色权限检查方法
 */
@Component
public class PermissionUtils {

    private static final Logger logger = LoggerFactory.getLogger(PermissionUtils.class);

    /**
     * 角色常量定义 - 与数据库保持一致
     */
    public static final Integer ROLE_ADMIN = 3;
    public static final Integer ROLE_STUDENT = 1;
    public static final Integer ROLE_REPAIRMAN = 2;

    private final JwtUtils jwtUtils;

    public PermissionUtils(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 检查是否为管理员
     *
     * @param token JWT令牌（可包含Bearer前缀）
     * @throws IllegalArgumentException 当权限不足时抛出
     */
    public void checkAdmin(String token) {
        Integer role = jwtUtils.getRoleFromTokenWithBearer(token);
        if (!ROLE_ADMIN.equals(role)) {
            logger.warn("权限不足: 需要管理员权限, 当前角色={}", role);
            throw new IllegalArgumentException("权限不足，需要管理员权限");
        }
    }

    /**
     * 检查是否为维修员
     *
     * @param token JWT令牌（可包含Bearer前缀）
     * @throws IllegalArgumentException 当权限不足时抛出
     */
    public void checkRepairman(String token) {
        Integer role = jwtUtils.getRoleFromTokenWithBearer(token);
        if (!ROLE_REPAIRMAN.equals(role)) {
            logger.warn("权限不足: 需要维修员权限, 当前角色={}", role);
            throw new IllegalArgumentException("权限不足，需要维修员权限");
        }
    }

    /**
     * 检查是否为学生
     *
     * @param token JWT令牌（可包含Bearer前缀）
     * @throws IllegalArgumentException 当权限不足时抛出
     */
    public void checkStudent(String token) {
        Integer role = jwtUtils.getRoleFromTokenWithBearer(token);
        if (!ROLE_STUDENT.equals(role)) {
            logger.warn("权限不足: 需要学生权限, 当前角色={}", role);
            throw new IllegalArgumentException("权限不足，需要学生权限");
        }
    }

    /**
     * 检查是否为管理员或维修员
     *
     * @param token JWT令牌（可包含Bearer前缀）
     * @throws IllegalArgumentException 当权限不足时抛出
     */
    public void checkAdminOrRepairman(String token) {
        Integer role = jwtUtils.getRoleFromTokenWithBearer(token);
        if (!ROLE_ADMIN.equals(role) && !ROLE_REPAIRMAN.equals(role)) {
            logger.warn("权限不足: 需要管理员或维修员权限, 当前角色={}", role);
            throw new IllegalArgumentException("权限不足，需要管理员或维修员权限");
        }
    }

    /**
     * 获取当前用户ID
     *
     * @param token JWT令牌（可包含Bearer前缀）
     * @return 用户ID
     * @throws IllegalArgumentException 当token无效时抛出
     */
    public Long getCurrentUserId(String token) {
        Long userId = jwtUtils.getUserIdFromTokenWithBearer(token);
        if (userId == null) {
            logger.warn("无效的token: 无法获取用户ID");
            throw new IllegalArgumentException("无效的token");
        }
        return userId;
    }

    /**
     * 获取当前用户角色
     *
     * @param token JWT令牌（可包含Bearer前缀）
     * @return 用户角色
     * @throws IllegalArgumentException 当token无效时抛出
     */
    public Integer getCurrentRole(String token) {
        Integer role = jwtUtils.getRoleFromTokenWithBearer(token);
        if (role == null) {
            logger.warn("无效的token: 无法获取用户角色");
            throw new IllegalArgumentException("无效的token");
        }
        return role;
    }

    /**
     * 验证并获取用户信息
     *
     * @param token JWT令牌（可包含Bearer前缀）
     * @return 用户信息对象
     */
    public UserInfo validateAndGetUserInfo(String token) {
        Long userId = jwtUtils.getUserIdFromTokenWithBearer(token);
        Integer role = jwtUtils.getRoleFromTokenWithBearer(token);
        
        if (userId == null || role == null) {
            throw new IllegalArgumentException("无效的token");
        }
        
        return new UserInfo(userId, role);
    }

    /**
     * 用户信息记录类
     */
    @Data
    public static class UserInfo {
        private final Long userId;
        private final Integer role;

        public UserInfo(Long userId, Integer role) {
            this.userId = userId;
            this.role = role;
        }
    }
}