package com.pxxy.houduan.controller;

import com.pxxy.houduan.common.PermissionUtils;
import com.pxxy.houduan.common.Result;
import com.pxxy.houduan.entity.FaultType;
import com.pxxy.houduan.entity.SysUser;
import com.pxxy.houduan.service.FaultTypeService;
import com.pxxy.houduan.service.StudentDormitoryService;
import com.pxxy.houduan.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生端控制器
 * 提供学生查询宿舍信息等功能的接口
 */
@RestController
@RequestMapping("/api/student")
@Tag(name = "学生端", description = "学生相关接口")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    private final PermissionUtils permissionUtils;
    private final StudentDormitoryService studentDormitoryService;
    private final FaultTypeService faultTypeService;
    private final SysUserService sysUserService;

    public StudentController(PermissionUtils permissionUtils, StudentDormitoryService studentDormitoryService, FaultTypeService faultTypeService, SysUserService sysUserService) {
        this.permissionUtils = permissionUtils;
        this.studentDormitoryService = studentDormitoryService;
        this.faultTypeService = faultTypeService;
        this.sysUserService = sysUserService;
    }

    /**
     * 获取当前学生的宿舍详细信息
     * 包括宿舍、楼栋、房间及维修员信息
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 返回宿舍详细信息，包含楼栋名称、房间号、维修员联系方式等
     */
    @Operation(summary = "获取我的宿舍信息", description = "获取当前学生的宿舍信息，包括楼栋名称、房间号、维修员联系方式等")
    @GetMapping("/dormitory")
    public Result<?> getMyDormitory(
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true)
            @RequestHeader("Authorization") String token) {
        permissionUtils.checkStudent(token);
        Long studentId = permissionUtils.getCurrentUserId(token);

        logger.info("学生查询宿舍信息: studentId={}", studentId);
        var result = studentDormitoryService.getStudentDormitoryDetails(studentId);
        return Result.success(result);
    }

    /**
     * 获取所有故障类型列表
     * 用于学生提交报修时选择故障类型
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 返回故障类型列表
     */
    @Operation(summary = "获取故障类型列表", description = "获取所有故障类型列表，用于学生提交报修时选择")
    @GetMapping("/fault-types")
    public Result<?> getFaultTypes(
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true)
            @RequestHeader("Authorization") String token) {
        permissionUtils.checkStudent(token);
        
        logger.info("学生查询故障类型列表");
        var result = faultTypeService.list();
        return Result.success(result);
    }

    /**
     * 获取所有用户列表（不包含自己）
     * 用于学生发起新聊天
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 返回用户列表
     */
    @Operation(summary = "获取所有用户列表", description = "获取所有用户列表（不包含自己），用于学生发起新聊天")
    @GetMapping("/users")
    public Result<?> getUserList(
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true)
            @RequestHeader("Authorization") String token) {
        permissionUtils.checkStudent(token);
        
        Long currentUserId = permissionUtils.getCurrentUserId(token);
        logger.info("学生查询所有用户列表，当前用户ID: {}", currentUserId);
        
        List<SysUser> users = sysUserService.list();
        users.removeIf(user -> user.getId().equals(currentUserId));
        
        // 输出日志看看用户数据
        users.forEach(user -> {
            logger.info("用户: id={}, username={}, realName={}, role={}", 
                user.getId(), user.getUsername(), user.getRealName(), user.getRole());
        });
        
        return Result.success(users);
    }
}