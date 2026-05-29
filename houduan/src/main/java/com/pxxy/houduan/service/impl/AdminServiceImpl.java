package com.pxxy.houduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pxxy.houduan.entity.*;
import com.pxxy.houduan.service.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务实现类
 * 提供对学生、维修员、楼栋、房间、故障类型的管理以及报修单详情查询和导出功能
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    /**
     * 验证班级格式：4位数字，如2201代表22届1班
     */
    private boolean isValidClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            return true; // 允许为空
        }
        return className.matches("^\\d{4}$");
    }

    private final SysUserService sysUserService;
    private final StudentDormitoryService studentDormitoryService;
    private final BuildingService buildingService;
    private final RoomService roomService;
    private final FaultTypeService faultTypeService;
    private final RepairOrderService repairOrderService;
    private final RepairRecordService repairRecordService;
    private final ExportService exportService;

    public AdminServiceImpl(SysUserService sysUserService,
                          StudentDormitoryService studentDormitoryService,
                          BuildingService buildingService,
                          RoomService roomService,
                          FaultTypeService faultTypeService,
                          RepairOrderService repairOrderService,
                          RepairRecordService repairRecordService,
                          ExportService exportService) {
        this.sysUserService = sysUserService;
        this.studentDormitoryService = studentDormitoryService;
        this.buildingService = buildingService;
        this.roomService = roomService;
        this.faultTypeService = faultTypeService;
        this.repairOrderService = repairOrderService;
        this.repairRecordService = repairRecordService;
        this.exportService = exportService;
    }

    /**
     * 添加新的维修员账号
     *
     * @param user 用户对象，包含用户名、密码等基本信息
     * @return 创建成功的维修员用户对象
     * @throws IllegalArgumentException 当用户名已存在时抛出
     */
    @Override
    public SysUser addRepairman(SysUser user) {
        // 设置角色为维修员(2)并启用账号
        user.setRole(2);
        user.setStatus(1);
        boolean success = sysUserService.register(user);
        if (!success) {
            throw new IllegalArgumentException("用户名已存在或添加失败");
        }
        logger.info("添加维修员成功: username={}", user.getUsername());
        return user;
    }

    /**
     * 修改指定维修员的信息
     *
     * @param id 维修员ID
     * @param user 更新后的用户信息
     * @return 更新后的维修员用户对象
     * @throws IllegalArgumentException 当维修员不存在或修改失败时抛出
     */
    @Override
    public SysUser updateRepairman(Long id, SysUser user) {
        // 验证维修员存在性并保留原密码
        SysUser existUser = sysUserService.getById(id);
        if (existUser == null || !existUser.getRole().equals(2)) {
            throw new IllegalArgumentException("维修员不存在");
        }
        user.setId(id);
        user.setRole(2);
        user.setPassword(existUser.getPassword());
        boolean success = sysUserService.updateById(user);
        if (!success) {
            throw new IllegalArgumentException("修改失败");
        }
        logger.info("修改维修员成功: id={}", id);
        return user;
    }

    /**
     * 切换维修员账号的启用/禁用状态
     *
     * @param id 维修员ID
     * @throws IllegalArgumentException 当维修员不存在或操作失败时抛出
     */
    @Override
    public void toggleRepairmanStatus(Long id) {
        // 获取当前状态并取反
        SysUser existUser = sysUserService.getById(id);
        if (existUser == null || !existUser.getRole().equals(2)) {
            throw new IllegalArgumentException("维修员不存在");
        }
        Integer newStatus = existUser.getStatus() == 1 ? 0 : 1;
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(newStatus);
        boolean success = sysUserService.updateById(user);
        if (!success) {
            throw new IllegalArgumentException("操作失败");
        }
        logger.info("{}维修员成功: id={}", newStatus == 1 ? "启用" : "禁用", id);
    }

    /**
     * 添加新的学生账号
     *
     * @param user 用户对象，包含用户名、密码等基本信息
     * @return 创建成功的学生用户对象
     * @throws IllegalArgumentException 当用户名已存在时抛出
     */
    @Override
    public SysUser addStudent(SysUser user) {
        // 验证班级格式
        if (!isValidClassName(user.getClassName())) {
            throw new IllegalArgumentException("班级格式错误，应为4位数字，如2201代表22届1班");
        }
        // 设置角色为学生(1)并启用账号
        user.setRole(1);
        user.setStatus(1);
        boolean success = sysUserService.register(user);
        if (!success) {
            throw new IllegalArgumentException("用户名已存在或添加失败");
        }
        logger.info("添加学生成功: username={}", user.getUsername());
        return user;
    }

    // ... existing code ...

    /**
     * 批量导入学生信息
     * 从Excel文件中读取学生数据，逐行验证后批量插入数据库
     * 支持事务回滚，任一学生导入失败不影响其他学生
     *
     * @param fileData Excel文件的字节数组数据
     * @return 包含导入统计信息的Map，包括：
     *         - totalCount: 总记录数
     *         - successCount: 成功导入数
     *         - failCount: 失败数
     *         - failMessages: 失败详情列表（包含行号和错误原因）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importStudents(byte[] fileData) {
        int total = 0;
        int success = 0;
        int failed = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        //ByteArrayInputStream将字节数组数据转换为输入流
        //创建一个XSSFWorkbook对象，用于处理Excel文件，并从输入流中加载Excel文件
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData))) {
            //获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            //获取工作表中的总行数
            total = sheet.getPhysicalNumberOfRows() - 1;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                //获取当前行
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    //获取当前行的数据
                    //row.getCell(0)获取当前行第0列的数据
                    //getCellValueAsString方法将单元格的值转换为字符串
                    String username = getCellValueAsString(row.getCell(0));
                    String realName = getCellValueAsString(row.getCell(1));
                    String password = getCellValueAsString(row.getCell(2));
                    String phone = getCellValueAsString(row.getCell(3));
                    String className = getCellValueAsString(row.getCell(4));

                    // 验证学号不能为空
                    if (username == null || username.trim().isEmpty()) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("row", i + 1);
                        error.put("原因", "学号不能为空");
                        errors.add(error);
                        failed++;
                        continue;
                    }

                    // 验证姓名不能为空
                    if (realName == null || realName.trim().isEmpty()) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("row", i + 1);
                        error.put("学号", username);
                        error.put("原因", "姓名不能为空");
                        errors.add(error);
                        failed++;
                        continue;
                    }

                    // 验证班级格式
                    if (!isValidClassName(className)) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("row", i + 1);
                        error.put("学号", username);
                        error.put("原因", "班级格式错误，应为4位数字，如2201代表22届1班");
                        errors.add(error);
                        failed++;
                        continue;
                    }

                    // 检查学号是否已存在
                    SysUser existUser = sysUserService.lambdaQuery()
                            .eq(SysUser::getUsername, username)
                            .one();
                    if (existUser != null) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("row", i + 1);
                        error.put("学号", username);
                        error.put("原因", "学号已存在");
                        errors.add(error);
                        failed++;
                        continue;
                    }

                    // 创建学生对象并设置默认值
                    SysUser student = new SysUser();
                    student.setUsername(username);
                    student.setRealName(realName);
                    student.setPassword(password != null && !password.isEmpty() ? password : "123456");
                    student.setPhone(phone);
                    student.setClassName(className);
                    student.setRole(1);
                    student.setStatus(1);
                    student.setCreateTime(LocalDateTime.now());

                    sysUserService.save(student);
                    success++;
                    logger.info("导入学生成功: username={}, realName={}", username, realName);

                } catch (Exception e) {
                    // 捕获单行数据处理异常，记录错误但不影响其他行
                    Map<String, Object> error = new HashMap<>();
                    error.put("row", i + 1);
                    error.put("原因", "数据解析错误: " + e.getMessage());
                    errors.add(error);
                    failed++;
                    logger.error("导入学生失败: 第{}行, 错误: {}", i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            // Excel文件解析失败，抛出异常触发事务回滚
            logger.error("解析Excel文件失败: {}", e.getMessage());
            throw new IllegalArgumentException("解析Excel文件失败: " + e.getMessage());
        }

        // 构建导入结果统计信息
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", total);
        result.put("successCount", success);
        result.put("failCount", failed);
        result.put("failMessages", errors);
        logger.info("批量导入学生完成: 总数={}, 成功={}, 失败={}", total, success, failed);

        return result;
    }

    /**
     * 下载学生信息导入模板
     * 生成包含表头和示例数据的Excel文件，供用户填写后批量导入
     *
     * @param response HTTP响应对象，用于输出Excel文件流
     * @throws RuntimeException 当模板生成或下载失败时抛出
     */
    @Override
    public void downloadStudentTemplate(jakarta.servlet.http.HttpServletResponse response) {
        //创建一个XSSFWorkbook对象，用于处理Excel文件
        try (Workbook workbook = new XSSFWorkbook()) {
            //创建一个工作簿对象，用于处理Excel文件
            Sheet sheet = workbook.createSheet("学生信息");

            // 创建表头样式（加粗字体）
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建表头行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号(必填)", "姓名(必填)", "初始密码(选填，默认123456)", "手机号(选填)", "班级(选填，格式如2201代表22届1班)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            String[] examples = {"2025001", "张三", "123456", "13800138001", "2201"};
            for (int i = 0; i < examples.length; i++) {
                exampleRow.createCell(i).setCellValue(examples[i]);
            }

            // 设置列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 25 * 256);
            }

            // 设置响应头，触发浏览器下载
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = "学生信息导入模板.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

            workbook.write(response.getOutputStream());
            logger.info("下载学生导入模板成功");
        } catch (Exception e) {
            logger.error("下载学生导入模板失败: {}", e.getMessage());
            throw new RuntimeException("下载模板失败: " + e.getMessage());
        }
    }

// ... existing code ...


    /**
     * 修改指定学生的信息
     *
     * @param id 学生ID
     * @param user 更新后的用户信息
     * @return 更新后的学生用户对象
     * @throws IllegalArgumentException 当学生不存在或修改失败时抛出
     */
    @Override
    public SysUser updateStudent(Long id, SysUser user) {
        // 验证学生存在性并保留原密码
        SysUser existUser = sysUserService.getById(id);
        if (existUser == null || !existUser.getRole().equals(1)) {
            throw new IllegalArgumentException("学生不存在");
        }
        // 验证班级格式
        if (!isValidClassName(user.getClassName())) {
            throw new IllegalArgumentException("班级格式错误，应为4位数字，如2201代表22届1班");
        }
        user.setId(id);
        user.setRole(1);
        user.setPassword(existUser.getPassword());
        boolean success = sysUserService.updateById(user);
        if (!success) {
            throw new IllegalArgumentException("修改失败");
        }
        logger.info("修改学生成功: id={}", id);
        return user;
    }

    /**
     * 切换学生账号的启用/禁用状态
     *
     * @param id 学生ID
     * @throws IllegalArgumentException 当学生不存在或操作失败时抛出
     */
    @Override
    public void toggleStudentStatus(Long id) {
        // 获取当前状态并取反
        SysUser existUser = sysUserService.getById(id);
        if (existUser == null || !existUser.getRole().equals(1)) {
            throw new IllegalArgumentException("学生不存在");
        }
        Integer newStatus = existUser.getStatus() == 1 ? 0 : 1;
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(newStatus);
        boolean success = sysUserService.updateById(user);
        if (!success) {
            throw new IllegalArgumentException("操作失败");
        }
        logger.info("{}学生成功: id={}", newStatus == 1 ? "启用" : "禁用", id);
    }

    /**
     * 为学生分配宿舍
     * 若学生已有宿舍则自动退宿后再分配新宿舍
     *
     * @param dormitory 宿舍分配对象，包含学生ID、房间ID
     * @throws IllegalArgumentException 当参数不完整、学生不存在或分配失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignDormitory(StudentDormitory dormitory) {
        // 校验参数完整性及学生有效性
        if (dormitory.getStudentId() == null || dormitory.getRoomId() == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        SysUser student = sysUserService.getById(dormitory.getStudentId());
        if (student == null || !student.getRole().equals(1)) {
            throw new IllegalArgumentException("学生不存在");
        }
        // 根据房间ID获取楼栋ID
        Room room = roomService.getById(dormitory.getRoomId());
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        dormitory.setBuildingId(room.getBuildingId());
        // 检查是否已有有效宿舍（状态为1），若有则先退宿
        StudentDormitory existing = studentDormitoryService.getActiveDormitoryByStudentId(dormitory.getStudentId());
        if (existing != null) {
            existing.setStatus(0);
            existing.setEndTime(LocalDateTime.now());
            studentDormitoryService.updateById(existing);
            logger.info("自动退宿: studentId={}, buildingId={}, roomId={}",
                    existing.getStudentId(), existing.getBuildingId(), existing.getRoomId());
        }
        // 保存新的宿舍分配记录
        dormitory.setStatus(1);
        dormitory.setCreateTime(LocalDateTime.now());
        boolean success = studentDormitoryService.save(dormitory);
        if (!success) {
            throw new IllegalArgumentException("分配失败");
        }
        logger.info("分配宿舍成功: studentId={}, buildingId={}, roomId={}",
                dormitory.getStudentId(), dormitory.getBuildingId(), dormitory.getRoomId());
    }

    /**
     * 办理学生退宿手续
     *
     * @param id 宿舍分配记录ID
     * @throws IllegalArgumentException 当记录不存在、已退宿或退宿失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkoutDormitory(Long id) {
        // 验证宿舍分配记录有效性
        StudentDormitory existing = studentDormitoryService.getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("宿舍分配记录不存在");
        }
        if (existing.getStatus() == 0) {
            throw new IllegalArgumentException("该学生已退宿");
        }
        // 更新状态为已退宿并设置结束时间
        existing.setStatus(0);
        existing.setEndTime(LocalDateTime.now());
        boolean success = studentDormitoryService.updateById(existing);
        if (!success) {
            throw new IllegalArgumentException("退宿失败");
        }
        logger.info("退宿成功: id={}, studentId={}", id, existing.getStudentId());
    }

    /**
     * 添加新的楼栋
     *
     * @param building 楼栋对象，包含楼栋名称等信息
     * @return 创建成功的楼栋对象
     * @throws IllegalArgumentException 当添加失败时抛出
     */
    @Override
    public Building addBuilding(Building building) {
        // 设置初始状态为启用并记录创建时间
        building.setStatus(1);
        building.setCreateTime(LocalDateTime.now());
        boolean success = buildingService.save(building);
        if (!success) {
            throw new IllegalArgumentException("添加失败");
        }
        logger.info("添加楼栋成功: buildingName={}", building.getBuildingName());
        return building;
    }

    /**
     * 修改指定楼栋的信息
     *
     * @param id 楼栋ID
     * @param building 更新后的楼栋信息
     * @return 更新后的楼栋对象
     * @throws IllegalArgumentException 当楼栋不存在或修改失败时抛出
     */
    @Override
    public Building updateBuilding(Long id, Building building) {
        // 验证楼栋存在性并设置更新时间
        Building existing = buildingService.getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("楼栋不存在");
        }
        building.setId(id);
        building.setUpdateTime(LocalDateTime.now());
        boolean success = buildingService.updateById(building);
        if (!success) {
            throw new IllegalArgumentException("修改失败");
        }
        logger.info("修改楼栋成功: id={}", id);
        return building;
    }

    /**
     * 切换楼栋的启用/禁用状态
     *
     * @param id 楼栋ID
     * @throws IllegalArgumentException 当楼栋不存在或操作失败时抛出
     */
    @Override
    public void toggleBuildingStatus(Long id) {
        // 获取当前状态并取反，同时更新修改时间
        Building existing = buildingService.getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("楼栋不存在");
        }
        Integer newStatus = existing.getStatus() == 1 ? 0 : 1;
        Building building = new Building();
        building.setId(id);
        building.setStatus(newStatus);
        building.setUpdateTime(LocalDateTime.now());
        boolean success = buildingService.updateById(building);
        if (!success) {
            throw new IllegalArgumentException("操作失败");
        }
        logger.info("{}楼栋成功: id={}", newStatus == 1 ? "启用" : "禁用", id);
    }

    @Override
    @Transactional
    public void deleteBuilding(Long id) {
        // 检查楼栋是否存在
        Building existing = buildingService.getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("楼栋不存在");
        }
        
        // 检查是否有房间关联
        LambdaQueryWrapper<Room> roomQuery = new LambdaQueryWrapper<>();
        roomQuery.eq(Room::getBuildingId, id);
        long roomCount = roomService.count(roomQuery);
        if (roomCount > 0) {
            throw new IllegalArgumentException("该楼栋下存在房间，无法删除");
        }
        
        // 检查是否有报修单关联
        LambdaQueryWrapper<RepairOrder> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(RepairOrder::getBuildingId, id);
        long orderCount = repairOrderService.count(orderQuery);
        if (orderCount > 0) {
            throw new IllegalArgumentException("该楼栋下存在报修单，无法删除");
        }
        
        // 删除楼栋
        boolean success = buildingService.removeById(id);
        if (!success) {
            throw new IllegalArgumentException("删除失败");
        }
        logger.info("删除楼栋成功: id={}", id);
    }

    /**
     * 添加新的房间
     *
     * @param room 房间对象，包含房间号、所属楼栋等信息
     * @return 创建成功的房间对象
     * @throws IllegalArgumentException 当楼栋不存在或添加失败时抛出
     */
    @Override
    public Room addRoom(Room room) {
        // 校验楼栋存在性并设置初始状态
        if (room.getBuildingId() == null) {
            throw new IllegalArgumentException("楼栋不能为空");
        }
        Building building = buildingService.getById(room.getBuildingId());
        if (building == null) {
            throw new IllegalArgumentException("楼栋不存在");
        }
        room.setStatus(1);
        room.setCreateTime(LocalDateTime.now());
        boolean success = roomService.save(room);
        if (!success) {
            throw new IllegalArgumentException("添加失败");
        }
        logger.info("添加房间成功: buildingId={}, roomNumber={}", room.getBuildingId(), room.getRoomNumber());
        return room;
    }

    /**
     * 修改指定房间的信息
     *
     * @param id 房间ID
     * @param room 更新后的房间信息
     * @return 更新后的房间对象
     * @throws IllegalArgumentException 当房间或楼栋不存在或修改失败时抛出
     */
    @Override
    public Room updateRoom(Long id, Room room) {
        // 验证房间存在性及关联楼栋有效性
        Room existing = roomService.getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        if (room.getBuildingId() != null) {
            Building building = buildingService.getById(room.getBuildingId());
            if (building == null) {
                throw new IllegalArgumentException("楼栋不存在");
            }
        }
        room.setId(id);
        boolean success = roomService.updateById(room);
        if (!success) {
            throw new IllegalArgumentException("修改失败");
        }
        logger.info("修改房间成功: id={}", id);
        return room;
    }

    /**
     * 切换房间的启用/禁用状态
     *
     * @param id 房间ID
     * @throws IllegalArgumentException 当房间不存在或操作失败时抛出
     */
    @Override
    public void toggleRoomStatus(Long id) {
        // 获取当前状态并取反
        Room existing = roomService.getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        Integer newStatus = existing.getStatus() == 1 ? 0 : 1;
        Room room = new Room();
        room.setId(id);
        room.setStatus(newStatus);
        boolean success = roomService.updateById(room);
        if (!success) {
            throw new IllegalArgumentException("操作失败");
        }
        logger.info("{}房间成功: id={}", newStatus == 1 ? "启用" : "禁用", id);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        // 检查房间是否存在
        Room existing = roomService.getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        
        // 检查是否有学生入住
        LambdaQueryWrapper<StudentDormitory> dormQuery = new LambdaQueryWrapper<>();
        dormQuery.eq(StudentDormitory::getRoomId, id);
        dormQuery.eq(StudentDormitory::getStatus, 1);
        long dormCount = studentDormitoryService.count(dormQuery);
        if (dormCount > 0) {
            throw new IllegalArgumentException("该房间有学生入住，无法删除");
        }
        
        // 检查是否有报修单关联
        LambdaQueryWrapper<RepairOrder> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(RepairOrder::getRoomId, id);
        long orderCount = repairOrderService.count(orderQuery);
        if (orderCount > 0) {
            throw new IllegalArgumentException("该房间下存在报修单，无法删除");
        }
        
        // 删除房间
        boolean success = roomService.removeById(id);
        if (!success) {
            throw new IllegalArgumentException("删除失败");
        }
        logger.info("删除房间成功: id={}", id);
    }

    /**
     * 添加新的故障类型
     *
     * @param faultType 故障类型对象，包含类型名称等信息
     * @return 创建成功的故障类型对象
     * @throws IllegalArgumentException 当添加失败时抛出
     */
    @Override
    public FaultType addFaultType(FaultType faultType) {
        // 设置初始状态为启用并记录创建时间
        faultType.setStatus(1);
        faultType.setCreateTime(LocalDateTime.now());
        boolean success = faultTypeService.save(faultType);
        if (!success) {
            throw new IllegalArgumentException("添加失败");
        }
        logger.info("添加故障类型成功: typeName={}", faultType.getTypeName());
        return faultType;
    }

    /**
     * 修改指定故障类型的信息
     *
     * @param id 故障类型ID
     * @param faultType 更新后的故障类型信息
     * @return 更新后的故障类型对象
     * @throws IllegalArgumentException 当故障类型不存在或修改失败时抛出
     */
    @Override
    public FaultType updateFaultType(Long id, FaultType faultType) {
        // 验证故障类型存在性
        FaultType existing = faultTypeService.getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("故障类型不存在");
        }
        faultType.setId(id);
        boolean success = faultTypeService.updateById(faultType);
        if (!success) {
            throw new IllegalArgumentException("修改失败");
        }
        logger.info("修改故障类型成功: id={}", id);
        return faultType;
    }

    /**
     * 切换故障类型的启用/禁用状态
     *
     * @param id 故障类型ID
     * @throws IllegalArgumentException 当故障类型不存在或操作失败时抛出
     */
    @Override
    public void toggleFaultTypeStatus(Long id) {
        // 获取当前状态并取反
        FaultType existing = faultTypeService.getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("故障类型不存在");
        }
        Integer newStatus = existing.getStatus() == 1 ? 0 : 1;
        FaultType faultType = new FaultType();
        faultType.setId(id);
        faultType.setStatus(newStatus);
        boolean success = faultTypeService.updateById(faultType);
        if (!success) {
            throw new IllegalArgumentException("操作失败");
        }
        logger.info("{}故障类型成功: id={}", newStatus == 1 ? "启用" : "禁用", id);
    }

    @Override
    public IPage<SysUser> listRepairmenPage(long current, long size) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getRole, 2);
        wrapper.orderByDesc(SysUser::getCreateTime);
        return sysUserService.page(new Page<>(current, size), wrapper);
    }

    @Override
    public IPage<SysUser> listStudentsPage(long current, long size, String className) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getRole, 1);
        if (className != null && !className.trim().isEmpty()) {
            wrapper.like(SysUser::getClassName, className);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return sysUserService.page(new Page<>(current, size), wrapper);
    }

    @Override
    public IPage<StudentDormitory> listDormitoriesPage(long current, long size) {
        LambdaQueryWrapper<StudentDormitory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StudentDormitory::getCreateTime);
        return studentDormitoryService.page(new Page<>(current, size), wrapper);
    }

    @Override
    public IPage<Room> listRoomsPage(Long buildingId, long current, long size) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        if (buildingId != null) {
            wrapper.eq(Room::getBuildingId, buildingId);
        }
        wrapper.orderByDesc(Room::getCreateTime);
        return roomService.page(new Page<>(current, size), wrapper);
    }

    /**
     * 查询指定报修单的详细信息
     * 包含关联的楼栋、房间、学生、维修员及维修记录
     *
     * @param orderId 订单ID
     * @return 包含完整信息的报修单详情Map
     * @throws IllegalArgumentException 当报修单不存在时抛出
     */
    @Override
    public Map<String, Object> getRepairOrderDetail(Long orderId) {
        // 验证报修单存在性并组装基础信息
        RepairOrder order = repairOrderService.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("报修单不存在");
        }

        logger.info("查询报修单详情: orderId={}", orderId);

        Map<String, Object> orderDetail = new HashMap<>();
        orderDetail.put("id", order.getId());
        orderDetail.put("orderNo", order.getOrderNo());
        orderDetail.put("studentId", order.getStudentId());
        orderDetail.put("buildingId", order.getBuildingId());
        orderDetail.put("roomId", order.getRoomId());
        orderDetail.put("faultTypeId", order.getFaultTypeId());
        orderDetail.put("description", order.getDescription());
        orderDetail.put("images", order.getImages());
        orderDetail.put("repairUserId", order.getRepairUserId());
        orderDetail.put("status", order.getStatus());
        orderDetail.put("rejectReason", order.getRejectReason());
        orderDetail.put("repairResult", order.getRepairResult());
        orderDetail.put("repairImages", order.getRepairImages());
        orderDetail.put("createTime", order.getCreateTime());
        orderDetail.put("acceptTime", order.getAcceptTime());
        orderDetail.put("completeTime", order.getCompleteTime());
        orderDetail.put("updateTime", order.getUpdateTime());

        // 关联查询楼栋名称
        if (order.getBuildingId() != null) {
            Building building = buildingService.getById(order.getBuildingId());
            if (building != null) {
                orderDetail.put("buildingName", building.getBuildingName());
            }
        }

        // 关联查询房间号
        if (order.getRoomId() != null) {
            Room room = roomService.getById(order.getRoomId());
            if (room != null) {
                orderDetail.put("roomNumber", room.getRoomNumber());
            }
        }

        // 关联查询故障类型名称
        if (order.getFaultTypeId() != null) {
            FaultType faultType = faultTypeService.getById(order.getFaultTypeId());
            if (faultType != null) {
                orderDetail.put("faultTypeName", faultType.getTypeName());
            }
        }

        // 关联查询学生信息
        if (order.getStudentId() != null) {
            SysUser student = sysUserService.getById(order.getStudentId());
            if (student != null) {
                orderDetail.put("studentName", student.getRealName());
                orderDetail.put("studentPhone", student.getPhone());
                orderDetail.put("studentClass", student.getClassName());
            }
        }

        // 关联查询维修员信息
        if (order.getRepairUserId() != null) {
            SysUser repairUser = sysUserService.getById(order.getRepairUserId());
            if (repairUser != null) {
                orderDetail.put("repairUserName", repairUser.getRealName());
                orderDetail.put("repairUserPhone", repairUser.getPhone());
            }
        }

        // 查询并组装维修记录列表
        List<RepairRecord> records = repairRecordService.getRecordsByOrderId(orderId);
        List<Map<String, Object>> recordList = new ArrayList<>();
        for (RepairRecord record : records) {
            Map<String, Object> recordDetail = new HashMap<>();
            recordDetail.put("id", record.getId());
            recordDetail.put("action", record.getAction());
            recordDetail.put("remark", record.getRemark());
            recordDetail.put("createTime", record.getCreateTime());

            if (record.getRepairUserId() != null) {
                SysUser repairUser = sysUserService.getById(record.getRepairUserId());
                if (repairUser != null) {
                    recordDetail.put("repairUserName", repairUser.getRealName());
                }
            }

            recordList.add(recordDetail);
        }
        orderDetail.put("repairRecords", recordList);

        return orderDetail;
    }

    /**
     * 导出指定时间范围内的报修单为Excel文件
     *
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @param response HTTP响应对象，用于输出Excel文件流
     * @throws RuntimeException 当导出失败时抛出
     */
    @Override
    public void exportRepairOrders(String startDate, String endDate, jakarta.servlet.http.HttpServletResponse response) {
        try {
            // 解析日期并调用导出服务生成工作簿
            // 支持多种日期格式：yyyy-MM-dd 和 ISO 8601格式（如 2026-05-24T16:00:00.000Z）
            LocalDate start = parseDate(startDate);
            LocalDate end = parseDate(endDate);

            Workbook workbook = exportService.exportRepairOrders(start, end);

            // 设置HTTP响应头以支持文件下载
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = "报修单记录_" + startDate + "_" + endDate + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");

            // 写入输出流并关闭工作簿
            workbook.write(response.getOutputStream());
            workbook.close();

            logger.info("导出报修单成功: startDate={}, endDate={}", startDate, endDate);
        } catch (Exception e) {
            logger.error("导出失败: {}", e.getMessage());
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    /**
     * 解析日期字符串，支持多种格式
     * @param dateStr 日期字符串，可以是 yyyy-MM-dd 或 ISO 8601格式
     * @return LocalDate 对象
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("日期不能为空");
        }
        // 移除可能的引号
        dateStr = dateStr.replace("\"", "").trim();
        // 如果是ISO 8601格式（包含T），提取日期部分
        if (dateStr.contains("T")) {
            dateStr = dateStr.substring(0, dateStr.indexOf("T"));
        }
        return LocalDate.parse(dateStr);
    }
}
