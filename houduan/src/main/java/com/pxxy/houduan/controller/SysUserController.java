package com.pxxy.houduan.controller;

import com.pxxy.houduan.common.JwtUtils;
import com.pxxy.houduan.common.Result;
import com.pxxy.houduan.entity.SysUser;
import com.pxxy.houduan.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统用户控制器
 * 提供用户登录、认证等功能的接口
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户登录、认证相关接口")
public class SysUserController {

    private static final Logger logger = LoggerFactory.getLogger(SysUserController.class);

    private final SysUserService sysUserService;
    private final JwtUtils jwtUtils;

    public SysUserController(SysUserService sysUserService, JwtUtils jwtUtils) {
        this.sysUserService = sysUserService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 用户登录接口
     * 验证用户名和密码，成功则生成JWT令牌并返回用户信息
     *
     * @param params 包含username和password的Map对象
     * @return 登录成功返回JWT令牌和用户信息，失败返回错误提示
     */
    @Operation(summary = "用户登录", description = "根据用户名密码登录系统，验证成功后返回JWT令牌和用户详细信息")
    @PostMapping("/login")
    public Result<?> login(
            @Parameter(description = "登录参数，包含username（用户名）和password（密码）", required = true)
            @RequestBody Map<String, String> params) {
        String username = params.get("username");
        logger.info("用户登录: username={}", username);

        // 调用服务层验证用户名和密码
        SysUser user = sysUserService.login(username, params.get("password"));
        if (user == null) {
            logger.warn("登录失败: 用户名或密码错误 - username={}", username);
            return Result.error("用户名或密码错误");
        }

        // 生成JWT令牌并添加Bearer前缀
        String rawToken = jwtUtils.generateToken(user.getId(), user.getRole());
        String token = "Bearer " + rawToken;

        logger.info("登录成功: username={}, role={}", username, user.getRole());
        //Map.of()是Java 9中引入的静态方法，用于创建不可变Map对象。
        logger.info("{}",user);
        //SysUser(id=5, username=20210002, password=123456, realName=李四,phone=13800138011,
        //              className=2202, role=1, status=1, createTime=2026-04-25T22:48:36, updateTime=2026-05-04T17:46:06)

        //map.of()创建一个不可变Map对象，并返回给调用者。{token=Bearer eyJhbGc... ,user=SysUser()}
        return Result.success(Map.of(
                "token", token,
                "user", user
        ));
    }

}
