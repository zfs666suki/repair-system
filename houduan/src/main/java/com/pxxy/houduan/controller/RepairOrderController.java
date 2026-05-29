package com.pxxy.houduan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pxxy.houduan.common.PageResult;
import com.pxxy.houduan.common.PermissionUtils;
import com.pxxy.houduan.common.Result;
import com.pxxy.houduan.entity.RepairOrder;
import com.pxxy.houduan.entity.SysUser;
import com.pxxy.houduan.service.RepairOrderService;
import com.pxxy.houduan.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 报修管理控制器
 * 提供报修单提交、处理、查询等功能的接口
 */
@RestController
@RequestMapping("/api/repair")
@Tag(name = "报修管理", description = "报修单提交、处理相关接口")
public class RepairOrderController {

    private static final Logger logger = LoggerFactory.getLogger(RepairOrderController.class);

    private final RepairOrderService repairOrderService;
    private final PermissionUtils permissionUtils;
    private final SysUserService sysUserService;

    public RepairOrderController(RepairOrderService repairOrderService, PermissionUtils permissionUtils, SysUserService sysUserService) {
        this.repairOrderService = repairOrderService;
        this.permissionUtils = permissionUtils;
        this.sysUserService = sysUserService;
    }

    /**
     * 学生提交报修单
     * 系统自动分配维修员
     *
     * @param order 报修单对象，包含故障类型、描述等信息
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 提交成功返回成功提示，失败返回错误信息
     */
    @Operation(summary = "提交报修单", description = "学生提交新的报修单，系统根据宿舍所属楼栋自动分配维修员")
    @PostMapping("/submit")
    public Result<?> submitOrder(
            @Parameter(description = "报修单信息", required = true)
            @RequestBody RepairOrder order,
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true)
            @RequestHeader("Authorization") String token) {
        permissionUtils.checkStudent(token);
        Long studentId = permissionUtils.getCurrentUserId(token);

        order.setStudentId(studentId);
        logger.info("提交报修单: studentId={}, faultTypeId={}, description={}",
                studentId, order.getFaultTypeId(), order.getDescription());

        boolean success = repairOrderService.submitOrder(order);
        if (success) {
            logger.info("报修提交成功: studentId={}", studentId);
            return Result.success("报修提交成功");
        }
        logger.warn("报修提交失败: studentId={}，可能未分配宿舍", studentId);
        return Result.error("报修提交失败，请确认是否已分配宿舍");
    }

    /**
     * 维修员接受报修任务
     *
     * @param params 请求参数，包含orderId（订单ID）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 接单成功返回成功提示，失败返回错误信息
     */
    @Operation(summary = "维修员接单", description = "维修员接受待接单状态的报修任务")
    @PostMapping("/accept")
    public Result<?> acceptOrder(
            @Parameter(description = "请求参数，包含orderId（订单ID，必填）", required = true)
            @RequestBody Map<String, Object> params,
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true)
            @RequestHeader("Authorization") String token) {
        permissionUtils.checkRepairman(token);
        Long repairUserId = permissionUtils.getCurrentUserId(token);

        Long orderId = getLongParam(params, "orderId");
        if (orderId == null) {
            return Result.error("订单ID格式错误");
        }
        logger.info("维修员接单: orderId={}, repairUserId={}", orderId, repairUserId);

        boolean success = repairOrderService.acceptOrder(orderId, repairUserId);
        if (success) {
            logger.info("接单成功: orderId={}", orderId);
            return Result.success("接单成功");
        }
        logger.warn("接单失败: orderId={}", orderId);
        return Result.error("接单失败");
    }

    /**
     * 维修员拒绝接单
     *
     * @param params 请求参数，包含orderId（订单ID）和reason（拒绝原因）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 拒单成功返回成功提示，失败返回错误信息
     */
    @Operation(summary = "维修员拒单", description = "维修员拒绝接单")
    @PostMapping("/reject")
    public Result<?> rejectOrder(@RequestBody Map<String, Object> params, @RequestHeader("Authorization") String token) {
        permissionUtils.checkRepairman(token);
        Long repairUserId = permissionUtils.getCurrentUserId(token);

        Long orderId = getLongParam(params, "orderId");
        String reason = params.get("reason") != null ? params.get("reason").toString() : null;
        if (orderId == null) {
            return Result.error("订单ID格式错误");
        }
        logger.info("维修员拒单: orderId={}, repairUserId={}, reason={}", orderId, repairUserId, reason);

        boolean success = repairOrderService.rejectOrder(orderId, repairUserId, reason);
        if (success) {
            logger.info("拒单成功: orderId={}", orderId);
            return Result.success("拒单成功");
        }
        logger.warn("拒单失败: orderId={}", orderId);
        return Result.error("拒单失败");
    }

    /**
     * 维修完成
     *
     * @param params 请求参数，包含orderId（订单ID）、repairResult（维修结果）和images（维修图片）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 维修完成返回成功提示，失败返回错误信息
     */
    @Operation(summary = "维修完成", description = "维修员完成维修，维修结果和维修图片都为必填项")
    @PostMapping("/complete")
    public Result<?> completeOrder(@RequestBody Map<String, Object> params, @RequestHeader("Authorization") String token) {
        permissionUtils.checkRepairman(token);

        Long orderId = getLongParam(params, "orderId");
        String result = params.get("repairResult") != null ? params.get("repairResult").toString() : null;
        String images = params.get("images") != null ? params.get("images").toString() : null;
        
        if (orderId == null) {
            return Result.error("订单ID格式错误");
        }
        if (result == null || result.trim().isEmpty()) {
            return Result.error("维修结果不能为空");
        }
        if (images == null || images.trim().isEmpty()) {
            return Result.error("维修图片不能为空");
        }
        
        logger.info("维修完成: orderId={}, result={}", orderId, result);

        boolean success = repairOrderService.completeOrder(orderId, result, images);
        if (success) {
            logger.info("维修完成成功: orderId={}", orderId);
            return Result.success("维修完成");
        }
        logger.warn("维修完成失败: orderId={}", orderId);
        return Result.error("维修完成失败，订单状态不正确");
    }

    /**
     * 学生更新报修单
     *
     * @param params 请求参数，包含orderId（订单ID）、faultTypeId（故障类型ID）、description（故障描述）、images（故障图片）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 更新成功返回成功提示，失败返回错误信息
     */
    @Operation(summary = "学生更新报修单", description = "学生更新已提交的报修单（仅可更新待分配或待接单状态）")
    @PostMapping("/update")
    public Result<?> updateOrder(@RequestBody Map<String, Object> params, @RequestHeader("Authorization") String token) {
        permissionUtils.checkStudent(token);
        Long studentId = permissionUtils.getCurrentUserId(token);

        Long orderId = getLongParam(params, "orderId");
        Long faultTypeId = getLongParam(params, "faultTypeId");
        String description = params.get("description") != null ? params.get("description").toString() : null;
        String images = params.get("images") != null ? params.get("images").toString() : null;
        
        if (orderId == null || faultTypeId == null || description == null) {
            return Result.error("参数不完整");
        }
        logger.info("学生更新报修单: orderId={}, studentId={}, faultTypeId={}", orderId, studentId, faultTypeId);

        boolean success = repairOrderService.updateOrder(orderId, faultTypeId, description, images, studentId);
        if (success) {
            logger.info("更新成功: orderId={}", orderId);
            return Result.success("更新成功");
        }
        logger.warn("更新失败: orderId={}", orderId);
        return Result.error("更新失败，请确认报修单状态是否允许修改");
    }

    /**
     * 学生取消报修
     *
     * @param params 请求参数，包含orderId（订单ID）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 取消成功返回成功提示，失败返回错误信息
     */
    @Operation(summary = "学生取消报修", description = "学生取消已提交的报修单")
    @PostMapping("/cancel")
    public Result<?> cancelOrder(@RequestBody Map<String, Object> params, @RequestHeader("Authorization") String token) {
        permissionUtils.checkStudent(token);
        Long studentId = permissionUtils.getCurrentUserId(token);

        String orderNo = params.get("orderNo") != null ? params.get("orderNo").toString() : null;
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return Result.error("订单编号不能为空");
        }
        logger.info("学生取消报修: orderNo={}, studentId={}", orderNo, studentId);

        boolean success = repairOrderService.cancelOrderByOrderNo(orderNo, studentId);
        if (success) {
            logger.info("取消成功: orderNo={}", orderNo);
            return Result.success("取消成功");
        }
        logger.warn("取消失败: orderNo={}", orderNo);
        return Result.error("取消失败，请确认报修单状态是否允许取消");
    }

    /**
     * 管理员分配维修员
     *
     * @param params 请求参数，包含orderId（订单ID）和repairUserId（维修员ID）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 分配成功返回成功提示，失败返回错误信息
     */
    @Operation(summary = "管理员分配维修员", description = "管理员手动分配维修员")
    @PostMapping("/assign")
    public Result<?> assignOrder(@RequestBody Map<String, Object> params, @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);

        Long orderId = getLongParam(params, "orderId");
        Long repairUserId = getLongParam(params, "repairUserId");
        if (orderId == null || repairUserId == null) {
            return Result.error("参数不完整");
        }
        logger.info("管理员分配: orderId={}, repairUserId={}", orderId, repairUserId);

        boolean success = repairOrderService.assignOrder(orderId, repairUserId);
        if (success) {
            logger.info("分配成功: orderId={}", orderId);
            return Result.success("分配成功");
        }
        logger.warn("分配失败: orderId={}", orderId);
        return Result.error("分配失败");
    }

    /**
     * 查询报修列表（支持多角色筛选）
     * 学生：只能查看自己的报修单
     * 维修员：只能查看分配给自己的报修单
     * 管理员：可以查看所有报修单
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param status 报修状态（可选）：0-待分配，1-待接单，2-维修中，3-已完成，4-已取消
     * @param buildingId 楼栋ID（可选）
     * @param faultTypeId 故障类型ID（可选）
     * @param startDate 开始日期（可选，格式yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式yyyy-MM-dd）
     * @param keyword 关键字（可选，搜索故障描述和维修结果）
     * @param token JWT认证令牌
     * @return 分页的报修列表
     */
    @Operation(summary = "查询报修列表", description = "获取报修单列表，支持多角色筛选、分页、筛选条件。学生只能查看自己的报修单，维修员只能查看分配给自己的报修单，管理员可以查看所有报修单")
    @GetMapping("/list")
    public PageResult<?> listOrders(
            @Parameter(description = "页码，默认1", required = false) @RequestParam(defaultValue = "1") long pageNum,
            @Parameter(description = "每页数量，默认10", required = false) @RequestParam(defaultValue = "10") long pageSize,
            @Parameter(description = "报修状态（0-待分配，1-待接单，2-维修中，3-已完成，4-已取消）", required = false) @RequestParam(required = false) Integer status,
            @Parameter(description = "楼栋ID", required = false) @RequestParam(required = false) Long buildingId,
            @Parameter(description = "故障类型ID", required = false) @RequestParam(required = false) Long faultTypeId,
            @Parameter(description = "开始日期（格式yyyy-MM-dd）", required = false) @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（格式yyyy-MM-dd）", required = false) @RequestParam(required = false) String endDate,
            @Parameter(description = "关键字（搜索故障描述和维修结果）", required = false) @RequestParam(required = false) String keyword,
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true) @RequestHeader("Authorization") String token) {
        Long userId = permissionUtils.getCurrentUserId(token);
        Integer role = permissionUtils.getCurrentRole(token);

        logger.info("查询报修列表: userId={}, role={}, status={}, buildingId={}, faultTypeId={}, keyword={}, pageNum={}, pageSize={}",
                userId, role, status, buildingId, faultTypeId, keyword, pageNum, pageSize);

        try {
            LocalDate startLocalDate = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate endLocalDate = endDate != null ? LocalDate.parse(endDate) : null;
            
            IPage<Map<String, Object>> page = repairOrderService.listOrdersWithDetailsPage(
                    status, buildingId, faultTypeId, startLocalDate, endLocalDate, keyword, userId, role, pageNum, pageSize);
            return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
        } catch (Exception e) {
            logger.error("日期格式错误: {}", e.getMessage());
            return PageResult.success(null, 0, pageNum, pageSize);
        }
    }

    /**
     * 查询报修单详情
     *
     * @param id 报修单ID
     * @param token JWT认证令牌
     * @return 报修单详情
     */
    @Operation(summary = "查询报修单详情", description = "根据订单ID查询报修单详情")
    @GetMapping("/detail/{id}")
    public Result<?> getRepairDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        logger.info("查询报修单详情: orderId={}", id);

        try {
            var result = repairOrderService.getRepairOrderDetails(id);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取Long类型参数
     */
    private Long getLongParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取所有用户列表（不包含自己）
     * 用于维修员发起新聊天
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 返回用户列表
     */
    @Operation(summary = "获取所有用户列表", description = "获取所有用户列表（不包含自己），用于维修员发起新聊天")
    @GetMapping("/users")
    public Result<?> getUserList(@RequestHeader("Authorization") String token) {
        permissionUtils.checkRepairman(token);

        Long currentUserId = permissionUtils.getCurrentUserId(token);
        logger.info("维修员查询所有用户列表，当前用户ID: {}", currentUserId);
        
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