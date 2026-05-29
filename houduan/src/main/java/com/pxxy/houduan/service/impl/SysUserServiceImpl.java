package com.pxxy.houduan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pxxy.houduan.config.WebSocketHandler;
import com.pxxy.houduan.entity.SysUser;
import com.pxxy.houduan.mapper.SysUserMapper;
import com.pxxy.houduan.service.SysUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统用户服务实现类
 * 提供用户登录和注册的业务逻辑实现
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final WebSocketHandler webSocketHandler;

    public SysUserServiceImpl(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * 用户登录方法
     * 根据用户名和密码验证用户身份，并返回有效的用户信息
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回SysUser对象，失败返回null
     */
    @Override
    public SysUser login(String username, String password) {
        // 查询数据库中匹配用户名、密码且状态为正常的用户
        SysUser user = this.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getPassword, password)
                .eq(SysUser::getStatus, 1)
                .one();
        return user;
    }

    /**
     * 用户注册方法
     * 检查用户名是否已存在，若不存在则创建新用户
     *
     * @param user 待注册的用户对象，需包含用户名等基本信息
     * @return 注册成功返回true，用户名已存在返回false
     */
    @Override
    public boolean register(SysUser user) {
        // 检查用户名是否已被注册
        SysUser existUser = this.lambdaQuery()
                .eq(SysUser::getUsername, user.getUsername())
                .one();
        if (existUser != null) {
            return false;
        }
        // 设置用户创建时间并保存到数据库
        user.setCreateTime(LocalDateTime.now());
        return this.save(user);
    }

    /**
     * 获取所有维修员列表
     *
     * @return 维修员用户列表
     */
    @Override
    public List<SysUser> getRepairmanList() {
        return this.lambdaQuery()
                .eq(SysUser::getRole, 2)  // role=2 表示维修员
                .eq(SysUser::getStatus, 1) // status=1 表示正常状态
                .list();
    }

    /**
     * 获取所有学生列表
     *
     * @return 学生用户列表
     */
    @Override
    public List<SysUser> getStudentList() {
        return this.lambdaQuery()
                .eq(SysUser::getRole, 1)  // role=1 表示学生
                .eq(SysUser::getStatus, 1) // status=1 表示正常状态
                .list();
    }

    /**
     * 获取所有在线维修员列表
     *
     * @return 在线维修员用户列表
     */
    @Override
    public List<SysUser> getOnlineRepairmanList() {
        Set<Long> onlineUserIds = webSocketHandler.getOnlineUserIds();
        if (onlineUserIds.isEmpty()) {
            return List.of();
        }
        return this.lambdaQuery()
                .eq(SysUser::getRole, 2)  // role=2 表示维修员
                .eq(SysUser::getStatus, 1) // status=1 表示正常状态
                .in(SysUser::getId, onlineUserIds)
                .list();
    }

    /**
     * 获取所有在线学生列表
     *
     * @return 在线学生用户列表
     */
    @Override
    public List<SysUser> getOnlineStudentList() {
        Set<Long> onlineUserIds = webSocketHandler.getOnlineUserIds();
        if (onlineUserIds.isEmpty()) {
            return List.of();
        }
        return this.lambdaQuery()
                .eq(SysUser::getRole, 1)  // role=1 表示学生
                .eq(SysUser::getStatus, 1) // status=1 表示正常状态
                .in(SysUser::getId, onlineUserIds)
                .list();
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    @Override
    public boolean isOnline(Long userId) {
        return webSocketHandler.isOnline(userId);
    }

}
