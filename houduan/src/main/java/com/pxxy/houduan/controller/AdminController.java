package com.pxxy.houduan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pxxy.houduan.common.JwtUtils;
import com.pxxy.houduan.common.PageResult;
import com.pxxy.houduan.common.PermissionUtils;
import com.pxxy.houduan.common.Result;
import com.pxxy.houduan.entity.*;
import com.pxxy.houduan.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 * 提供管理员对学生、维修员、楼栋、房间、故障类型等的管理操作及统计功能
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员管理", description = "管理员对学生、维修员、楼栋、房间等的管理操作")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;
    private final SysUserService sysUserService;
    private final StudentDormitoryService studentDormitoryService;
    private final BuildingService buildingService;
    private final RoomService roomService;
    private final FaultTypeService faultTypeService;
    private final StatisticsService statisticsService;
    private final RepairOrderService repairOrderService;
    private final RepairRecordService repairRecordService;
    private final JwtUtils jwtUtils;
    private final PermissionUtils permissionUtils;

    public AdminController(AdminService adminService,
                         SysUserService sysUserService,
                         StudentDormitoryService studentDormitoryService,
                         BuildingService buildingService,
                         RoomService roomService,
                         FaultTypeService faultTypeService,
                         StatisticsService statisticsService,
                         RepairOrderService repairOrderService,
                         RepairRecordService repairRecordService,
                         JwtUtils jwtUtils,
                         PermissionUtils permissionUtils) {
        this.adminService = adminService;
        this.sysUserService = sysUserService;
        this.studentDormitoryService = studentDormitoryService;
        this.buildingService = buildingService;
        this.roomService = roomService;
        this.faultTypeService = faultTypeService;
        this.statisticsService = statisticsService;
        this.repairOrderService = repairOrderService;
        this.repairRecordService = repairRecordService;
        this.jwtUtils = jwtUtils;
        this.permissionUtils = permissionUtils;
    }

    /**
     * 查询所有维修员列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 维修员用户列表
     */
    @GetMapping("/repairmen")
    @Operation(summary = "查询维修员列表", description = "获取维修员用户信息列表，支持分页")
    public PageResult<?> listRepairmen(
            @Parameter(description = "页码，默认1", required = false) @RequestParam(defaultValue = "1") long pageNum,
            @Parameter(description = "每页数量，默认10", required = false) @RequestParam(defaultValue = "10") long pageSize,
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true) @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        IPage<SysUser> page = adminService.listRepairmenPage(pageNum, pageSize);
        logger.info("查询维修员列表: pageNum={}, pageSize={}, total={}", pageNum, pageSize, page.getTotal());
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 创建新的维修员账号
     *
     * @param user 维修员用户对象，包含用户名、密码等信息
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PostMapping("/repairmen")
    @Operation(summary = "添加维修员", description = "创建新的维修员账号")
    public Result<?> addRepairman(
            @Parameter(description = "维修员用户对象，包含username（用户名）、password（密码）、realName（真实姓名）、phone（电话）等", required = true) @RequestBody SysUser user,
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true) @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.addRepairman(user);
        return Result.success("添加维修员成功");
    }

    /**
     * 修改指定维修员的信息
     *
     * @param id 维修员ID
     * @param user 更新后的用户信息
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PutMapping("/repairmen/{id}")
    @Operation(summary = "修改维修员", description = "修改指定维修员的信息")
    public Result<?> updateRepairman(
            @Parameter(description = "维修员ID", required = true) @PathVariable Long id,
            @Parameter(description = "更新后的用户信息", required = true) @RequestBody SysUser user,
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true) @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.updateRepairman(id, user);
        return Result.success("修改维修员成功");
    }

    /**
     * 切换维修员账号的启用/禁用状态
     *
     * @param id 维修员ID
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PutMapping("/repairmen/{id}/toggle")
    @Operation(summary = "切换维修员状态", description = "自动切换维修员账号的启用/禁用状态")
    public Result<?> toggleRepairmanStatus(
            @Parameter(description = "维修员ID", required = true) @PathVariable Long id,
            @Parameter(description = "JWT认证令牌，格式为Bearer {token}", required = true) @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.toggleRepairmanStatus(id);
        return Result.success("操作成功");
    }

    /**
     * 查询所有学生列表
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 学生用户列表
     */
    @GetMapping("/students")
    @Operation(summary = "查询学生列表", description = "获取学生信息，支持分页和班级筛选")
    public PageResult<?> listStudents(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String className,
            @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        IPage<SysUser> page = adminService.listStudentsPage(pageNum, pageSize, className);
        logger.info("查询学生列表: pageNum={}, pageSize={}, className={}, total={}", pageNum, pageSize, className, page.getTotal());
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 创建新的学生账号
     *
     * @param user 学生用户对象，包含用户名、密码等信息
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PostMapping("/students")
    @Operation(summary = "添加学生", description = "创建新的学生账号")
    public Result<?> addStudent(@RequestBody SysUser user, @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.addStudent(user);
        return Result.success("添加学生成功");
    }

    /**
     * 下载学生批量导入的Excel模板文件
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @param response HTTP响应对象，用于输出Excel模板文件流
     */
    @GetMapping("/students/template")
    @Operation(summary = "下载学生导入模板", description = "下载Excel模板文件")
    public void downloadStudentTemplate(@RequestHeader("Authorization") String token, HttpServletResponse response) {
        permissionUtils.checkAdmin(token);
        adminService.downloadStudentTemplate(response);
    }

    /**
     * 通过Excel文件批量导入学生数据
     *
     * @param file 上传的Excel文件，包含学生信息
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 导入结果统计信息，包含成功数和失败数等
     * @throws IOException 当文件读取失败时抛出
     */
    @PostMapping("/students/import")
    @Operation(summary = "批量导入学生", description = "通过Excel文件批量导入学生")
    //MultipartFile是Spring提供的用于处理文件上传的类
    public Result<?> importStudents(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) throws IOException {
        permissionUtils.checkAdmin(token);
        try {
            // 将文件数据转换为字节数组
            byte[] fileData = file.getBytes();
            Map<String, Object> result = adminService.importStudents(fileData);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }


    /**
     * 修改指定学生的信息
     *
     * @param id 学生ID
     * @param user 更新后的用户信息
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PutMapping("/students/{id}")
    @Operation(summary = "修改学生", description = "修改学生信息")
    public Result<?> updateStudent(@PathVariable Long id,
                                  @RequestBody SysUser user, @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.updateStudent(id, user);
        return Result.success("修改学生成功");
    }

    /**
     * 切换学生账号的启用/禁用状态
     *
     * @param id 学生ID
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PutMapping("/students/{id}/toggle")
    @Operation(summary = "切换学生状态", description = "自动切换学生账号状态")
    public Result<?> toggleStudentStatus(@PathVariable Long id,
                                       @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.toggleStudentStatus(id);
        return Result.success("操作成功");
    }

    /**
     * 查询所有宿舍分配记录（关联查询学生、楼栋、房间信息）
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 宿舍分配列表，包含学生姓名和宿舍信息
     */
    @GetMapping("/dormitories")
    @Operation(summary = "查询宿舍分配列表", description = "获取宿舍分配信息，支持分页，包含学生姓名和宿舍信息")
    public PageResult<?> listDormitories(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        IPage<StudentDormitory> page = adminService.listDormitoriesPage(pageNum, pageSize);
        
        // 关联查询学生姓名、楼栋名称和房间号
        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentDormitory dormitory : page.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", dormitory.getId());
            map.put("studentId", dormitory.getStudentId());
            map.put("buildingId", dormitory.getBuildingId());
            map.put("roomId", dormitory.getRoomId());
            map.put("status", dormitory.getStatus());
            map.put("createTime", dormitory.getCreateTime());
            map.put("endTime", dormitory.getEndTime());
            
            // 查询学生姓名
            if (dormitory.getStudentId() != null) {
                SysUser student = sysUserService.getById(dormitory.getStudentId());
                if (student != null) {
                    map.put("studentName", student.getRealName());
                    map.put("username", student.getUsername());
                } else {
                    map.put("studentName", "未知学生");
                    map.put("username", "");
                }
            } else {
                map.put("studentName", "未知学生");
                map.put("username", "");
            }
            
            // 查询楼栋名称和房间号
            String dormitoryInfo = "未知房间";
            if (dormitory.getBuildingId() != null && dormitory.getRoomId() != null) {
                Building building = buildingService.getById(dormitory.getBuildingId());
                Room room = roomService.getById(dormitory.getRoomId());
                if (building != null && room != null) {
                    dormitoryInfo = building.getBuildingName() + room.getRoomNumber();
                } else if (building != null) {
                    dormitoryInfo = building.getBuildingName() + "未知房间";
                } else if (room != null) {
                    dormitoryInfo = "未知楼栋" + room.getRoomNumber();
                }
            }
            map.put("dormitoryInfo", dormitoryInfo);
            
            result.add(map);
        }
        
        logger.info("查询宿舍分配列表: pageNum={}, pageSize={}, total={}", pageNum, pageSize, page.getTotal());
        return PageResult.success(result, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 为学生分配宿舍
     *
     * @param dormitory 宿舍分配对象，包含学生ID、楼栋ID、房间ID
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PostMapping("/dormitories")
    @Operation(summary = "分配宿舍", description = "为学生分配宿舍")
    public Result<?> assignDormitory(@RequestBody StudentDormitory dormitory,
                                     @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.assignDormitory(dormitory);
        return Result.success("分配宿舍成功");
    }

    /**
     * 办理学生退宿手续
     *
     * @param id 宿舍分配记录ID
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PutMapping("/dormitories/{id}/checkout")
    @Operation(summary = "退宿舍", description = "学生退宿")
    public Result<?> checkoutDormitory(@PathVariable Long id,
                                      @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.checkoutDormitory(id);
        return Result.success("退宿成功");
    }

    /**
     * 获取所有用户列表（不包含自己）
     * 用于管理员发起新聊天
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 返回用户列表
     */
    @GetMapping("/users")
    @Operation(summary = "获取所有用户列表", description = "获取所有用户列表（不包含自己），用于管理员发起新聊天")
    public Result<?> getUserList(@RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        
        Long currentUserId = permissionUtils.getCurrentUserId(token);
        logger.info("管理员查询所有用户列表，当前用户ID: {}", currentUserId);
        
        List<SysUser> users = sysUserService.list();
        users.removeIf(user -> user.getId().equals(currentUserId));
        
        return Result.success(users);
    }

    /**
     * 查询所有楼栋列表（关联维修员信息）
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 楼栋列表，包含维修员姓名
     */
    @GetMapping("/buildings")
    @Operation(summary = "查询楼栋列表", description = "获取所有楼栋信息，包含维修员姓名")
    public Result<?> listBuildings(@RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        var buildings = buildingService.list();
        
        // 关联查询维修员姓名
        List<Map<String, Object>> result = new ArrayList<>();
        for (Building building : buildings) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", building.getId());
            map.put("buildingName", building.getBuildingName());
            map.put("repairUserId", building.getRepairUserId());
            
            // 查询维修员姓名
            if (building.getRepairUserId() != null) {
                SysUser repairman = sysUserService.getById(building.getRepairUserId());
                if (repairman != null) {
                    map.put("repairUserName", repairman.getRealName());
                } else {
                    map.put("repairUserName", "");
                }
            } else {
                map.put("repairUserName", "");
            }
            
            map.put("status", building.getStatus());
            map.put("createTime", building.getCreateTime());
            map.put("updateTime", building.getUpdateTime());
            result.add(map);
        }
        
        logger.info("查询楼栋列表: count={}", result.size());
        return Result.success(result);
    }

    /**
     * 添加新的楼栋
     *
     * @param building 楼栋对象，包含楼栋名称等信息
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PostMapping("/buildings")
    @Operation(summary = "添加楼栋", description = "添加新的楼栋")
    public Result<?> addBuilding(@RequestBody Building building, @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.addBuilding(building);
        return Result.success("添加楼栋成功");
    }

    /**
     * 修改指定楼栋的信息
     *
     * @param id 楼栋ID
     * @param building 更新后的楼栋信息
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PutMapping("/buildings/{id}")
    @Operation(summary = "修改楼栋", description = "修改楼栋信息")
    public Result<?> updateBuilding(@PathVariable Long id,
                                   @RequestBody Building building, @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.updateBuilding(id, building);
        return Result.success("修改楼栋成功");
    }

    /**
     * 切换楼栋的启用/禁用状态
     *
     * @param id 楼栋ID
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PutMapping("/buildings/{id}/toggle")
    @Operation(summary = "切换楼栋状态", description = "自动切换楼栋状态")
    public Result<?> toggleBuildingStatus(@PathVariable Long id,
                                        @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.toggleBuildingStatus(id);
        return Result.success("操作成功");
    }

    /**
     * 删除指定楼栋
     *
     * @param id 楼栋ID
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @DeleteMapping("/buildings/{id}")
    @Operation(summary = "删除楼栋", description = "删除指定楼栋")
    public Result<?> deleteBuilding(@PathVariable Long id,
                                   @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.deleteBuilding(id);
        return Result.success("删除楼栋成功");
    }

    /**
     * 查询所有房间列表（关联楼栋信息）
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 房间列表，包含楼栋名称
     */
    @GetMapping("/rooms")
    @Operation(summary = "查询房间列表", description = "获取房间信息，支持分页和楼栋筛选，包含楼栋名称")
    public PageResult<?> listRooms(
            @RequestParam(required = false) Long buildingId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        IPage<Room> page = adminService.listRoomsPage(buildingId, pageNum, pageSize);
        
        // 关联查询楼栋名称
        List<Map<String, Object>> result = new ArrayList<>();
        for (Room room : page.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", room.getId());
            map.put("buildingId", room.getBuildingId());
            
            // 查询楼栋名称
            if (room.getBuildingId() != null) {
                Building building = buildingService.getById(room.getBuildingId());
                if (building != null) {
                    map.put("buildingName", building.getBuildingName());
                } else {
                    map.put("buildingName", "未知楼栋");
                }
            } else {
                map.put("buildingName", "未知楼栋");
            }
            
            map.put("roomNumber", room.getRoomNumber());
            map.put("status", room.getStatus());
            map.put("createTime", room.getCreateTime());
            result.add(map);
        }
        
        logger.info("查询房间列表: buildingId={}, pageNum={}, pageSize={}, total={}", buildingId, pageNum, pageSize, page.getTotal());
        return PageResult.success(result, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 添加新的房间
     *
     * @param room 房间对象，包含房间号、所属楼栋等信息
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PostMapping("/rooms")
    @Operation(summary = "添加房间", description = "添加新的房间")
    public Result<?> addRoom(@RequestBody Room room, @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.addRoom(room);
        return Result.success("添加房间成功");
    }

    /**
     * 修改指定房间的信息
     *
     * @param id 房间ID
     * @param room 更新后的房间信息
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PutMapping("/rooms/{id}")
    @Operation(summary = "修改房间", description = "修改房间信息")
    public Result<?> updateRoom(@PathVariable Long id,
                              @RequestBody Room room, @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.updateRoom(id, room);
        return Result.success("修改房间成功");
    }

    /**
     * 切换房间的启用/禁用状态
     *
     * @param id 房间ID
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PutMapping("/rooms/{id}/toggle")
    @Operation(summary = "切换房间状态", description = "自动切换房间状态")
    public Result<?> toggleRoomStatus(@PathVariable Long id,
                                    @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.toggleRoomStatus(id);
        return Result.success("操作成功");
    }

    /**
     * 删除指定房间
     *
     * @param id 房间ID
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @DeleteMapping("/rooms/{id}")
    @Operation(summary = "删除房间", description = "删除指定房间")
    public Result<?> deleteRoom(@PathVariable Long id,
                               @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.deleteRoom(id);
        return Result.success("删除房间成功");
    }

    /**
     * 查询所有故障类型列表
     *
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 故障类型列表
     */
    @GetMapping("/fault-types")
    @Operation(summary = "查询故障类型列表", description = "获取所有故障类型信息")
    public Result<?> listFaultTypes(@RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        var faultTypes = faultTypeService.list();
        logger.info("查询故障类型列表: count={}", faultTypes.size());
        return Result.success(faultTypes);
    }

    /**
     * 添加新的故障类型
     *
     * @param faultType 故障类型对象，包含类型名称等信息
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PostMapping("/fault-types")
    @Operation(summary = "添加故障类型", description = "添加新的故障类型")
    public Result<?> addFaultType(@RequestBody FaultType faultType, @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.addFaultType(faultType);
        return Result.success("添加故障类型成功");
    }

    /**
     * 修改指定故障类型的信息
     *
     * @param id 故障类型ID
     * @param faultType 更新后的故障类型信息
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PutMapping("/fault-types/{id}")
    @Operation(summary = "修改故障类型", description = "修改故障类型信息")
    public Result<?> updateFaultType(@PathVariable Long id,
                                     @RequestBody FaultType faultType, @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.updateFaultType(id, faultType);
        return Result.success("修改故障类型成功");
    }

    /**
     * 切换故障类型的启用/禁用状态
     *
     * @param id 故障类型ID
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 操作结果提示
     */
    @PutMapping("/fault-types/{id}/toggle")
    @Operation(summary = "切换故障类型状态", description = "自动切换故障类型状态")
    public Result<?> toggleFaultTypeStatus(@PathVariable Long id,
                                         @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        adminService.toggleFaultTypeStatus(id);
        return Result.success("操作成功");
    }

    /**
     * 按日期统计报修单数量
     *
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 按日期分组的统计数据
     */
    @GetMapping("/statistics/by-date")
    @Operation(summary = "按日期统计报修", description = "按日期统计报修数量")
    public Result<?> statisticsByDate(@RequestParam String startDate,
                                     @RequestParam String endDate,
                                     @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        var result = statisticsService.statisticsByDate(start, end);
        logger.info("按日期统计报修: startDate={}, endDate={}, count={}", startDate, endDate, result.size());
        return Result.success(result);
    }

    /**
     * 按楼栋统计报修单数量
     *
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 按楼栋分组的统计数据
     */
    @GetMapping("/statistics/by-building")
    @Operation(summary = "按楼栋统计报修", description = "按楼栋统计报修数量")
    public Result<?> statisticsByBuilding(@RequestParam String startDate,
                                         @RequestParam String endDate,
                                         @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        var result = statisticsService.statisticsByBuilding(start, end);
        logger.info("按楼栋统计报修: startDate={}, endDate={}, count={}", startDate, endDate, result.size());
        return Result.success(result);
    }

    /**
     * 按故障类型统计报修单数量
     *
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 按故障类型分组的统计数据
     */
    @GetMapping("/statistics/by-fault-type")
    @Operation(summary = "按故障类型统计报修", description = "按故障类型统计报修数量")
    public Result<?> statisticsByFaultType(@RequestParam String startDate,
                                          @RequestParam String endDate,
                                          @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        var result = statisticsService.statisticsByFaultType(start, end);
        logger.info("按故障类型统计报修: startDate={}, endDate={}, count={}", startDate, endDate, result.size());
        return Result.success(result);
    }

    /**
     * 按报修状态统计报修单数量
     *
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 按状态分组的统计数据
     */
    @GetMapping("/statistics/by-status")
    @Operation(summary = "按状态统计报修", description = "按状态统计报修数量")
    public Result<?> statisticsByStatus(@RequestParam String startDate,
                                       @RequestParam String endDate,
                                       @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        var result = statisticsService.statisticsByStatus(start, end);
        logger.info("按状态统计报修: startDate={}, endDate={}, count={}", startDate, endDate, result.size());
        return Result.success(result);
    }

    /**
     * 获取报修统计汇总信息
     *
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 统计汇总数据
     */
    @GetMapping("/statistics/summary")
    @Operation(summary = "获取报修统计汇总", description = "获取报修统计汇总信息")
    public Result<?> getStatisticsSummary(@RequestParam String startDate,
                                         @RequestParam String endDate,
                                         @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        var result = statisticsService.getStatisticsSummary(start, end);
        logger.info("获取报修统计汇总: startDate={}, endDate={}", startDate, endDate);
        return Result.success(result);
    }

    /**
     * 导出指定时间范围内的报修单为Excel文件
     *
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @param response HTTP响应对象，用于输出Excel文件流
     */
    @GetMapping("/export/repair-orders")
    @Operation(summary = "导出报修单为Excel", description = "导出指定时间范围内的报修单为Excel文件")
    public void exportRepairOrders(@RequestParam String startDate,
                                   @RequestParam String endDate,
                                   @RequestHeader("Authorization") String token,
                                   HttpServletResponse response) {
        permissionUtils.checkAdmin(token);
        adminService.exportRepairOrders(startDate, endDate, response);
    }

    /**
     * 查询指定报修单的详细信息
     *
     * @param orderId 订单ID
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 报修单详细信息，包含关联的楼栋、学生、维修员等
     */
    @GetMapping("/repair-order/{orderId}")
    @Operation(summary = "查询报修单详情", description = "根据订单ID查询报修单的详细信息")
    public Result<?> getRepairOrderDetail(@PathVariable Long orderId,
                                        @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        Map<String, Object> detail = adminService.getRepairOrderDetail(orderId);
        return Result.success(detail);
    }

    /**
     * 获取所有维修员在特定时间段内的统计列表
     *
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @param token JWT认证令牌，格式为"Bearer {token}"
     * @return 所有维修员的统计数据列表
     */
    @GetMapping("/statistics/repair-users")
    @Operation(summary = "获取所有维修员的统计列表", description = "获取所有维修员的统计信息列表")
    public Result<?> getAllRepairUserStatistics(@RequestParam String startDate,
                                               @RequestParam String endDate,
                                               @RequestHeader("Authorization") String token) {
        permissionUtils.checkAdmin(token);
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        List<Map<String, Object>> statisticsList = statisticsService.getAllRepairUserStatistics(start, end);
        logger.info("获取所有维修员统计列表: startDate={}, endDate={}", startDate, endDate);
        return Result.success(statisticsList);
    }

    /**
     * 解析日期字符串，支持多种格式
     * 支持格式：yyyy-MM-dd, yyyy-MM-ddTHH:mm:ss.SSSZ, yyyy-MM-ddTHH:mm:ss
     *
     * @param dateStr 日期字符串
     * @return LocalDate对象
     * @throws IllegalArgumentException 当日期格式不支持时抛出
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("日期不能为空");
        }
        
        // 支持的日期格式
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ISO_LOCAL_DATE,  // yyyy-MM-dd
            DateTimeFormatter.ISO_DATE_TIME,   // yyyy-MM-ddTHH:mm:ss.SSS
            DateTimeFormatter.ISO_OFFSET_DATE_TIME  // yyyy-MM-ddTHH:mm:ss.SSSZ
        };
        
        // 尝试各种格式解析
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {
                // 继续尝试下一个格式
            }
        }
        
        // 如果都不行，尝试截取前10个字符（yyyy-MM-dd部分）
        if (dateStr.length() >= 10) {
            String datePart = dateStr.substring(0, 10);
            try {
                return LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) {
            }
        }
        
        throw new IllegalArgumentException("不支持的日期格式: " + dateStr);
    }

}
