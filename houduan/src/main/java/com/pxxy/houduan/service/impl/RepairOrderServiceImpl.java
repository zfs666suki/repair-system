package com.pxxy.houduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pxxy.houduan.entity.*;
import com.pxxy.houduan.entity.Room;
import com.pxxy.houduan.mapper.BuildingMapper;
import com.pxxy.houduan.mapper.FaultTypeMapper;
import com.pxxy.houduan.mapper.RepairOrderMapper;
import com.pxxy.houduan.mapper.RepairRecordMapper;
import com.pxxy.houduan.mapper.SysUserMapper;
import com.pxxy.houduan.service.NotificationService;
import com.pxxy.houduan.service.RepairOrderService;
import com.pxxy.houduan.service.RoomService;
import com.pxxy.houduan.service.StudentDormitoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报修单服务实现类
 * 提供报修单提交、处理、查询、分配等业务逻辑
 */
@Service
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {

    private static final Logger logger = LoggerFactory.getLogger(RepairOrderServiceImpl.class);
    private static final Long ADMIN_ID = 1L; // 管理员ID

    private final BuildingMapper buildingMapper;
    private final RepairRecordMapper repairRecordMapper;
    private final StudentDormitoryService studentDormitoryService;
    private final SysUserMapper sysUserMapper;
    private final NotificationService notificationService;
    private final FaultTypeMapper faultTypeMapper;
    private final RoomService roomService;

    public RepairOrderServiceImpl(BuildingMapper buildingMapper, RepairRecordMapper repairRecordMapper,
                                  StudentDormitoryService studentDormitoryService, SysUserMapper sysUserMapper,
                                  NotificationService notificationService, FaultTypeMapper faultTypeMapper,
                                  RoomService roomService) {
        this.buildingMapper = buildingMapper;
        this.repairRecordMapper = repairRecordMapper;
        this.studentDormitoryService = studentDormitoryService;
        this.sysUserMapper = sysUserMapper;
        this.notificationService = notificationService;
        this.faultTypeMapper = faultTypeMapper;
        this.roomService = roomService;
    }

    /**
     * 学生提交报修单
     * 自动获取宿舍信息并生成订单号，系统尝试自动分配维修员
     *
     * @param order 报修单对象，包含学生ID、故障类型、描述等信息
     * @return 提交成功返回true，失败返回false（通常因未分配宿舍）
     */
    @Override
    @Transactional
    public boolean submitOrder(RepairOrder order) {
        // 获取学生宿舍信息并设置楼栋和房间ID
        StudentDormitory dormitory = studentDormitoryService.getActiveDormitoryByStudentId(order.getStudentId());
        if (dormitory == null) {
            return false;
        }
        order.setBuildingId(dormitory.getBuildingId());
        order.setRoomId(dormitory.getRoomId());

        // 生成订单号并设置初始状态
        order.setOrderNo("BX" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        order.setStatus(1);
        order.setCreateTime(LocalDateTime.now());
        //save是mybatis-plus提供的方法,用于保存数据
        //其中this是当前类对象，即repairOrderServiceImpl
        //this.save(order)是保存order对象到数据库中 true表示保存成功 false表示保存失败
        if (!this.save(order)) {
            return false;
        }

        // 获取学生信息用于消息通知
        SysUser student = sysUserMapper.selectById(order.getStudentId());
        String studentName = student != null ? student.getRealName() : "学生";

        // 尝试自动分配维修员并发送通知
        Building building = buildingMapper.selectById(order.getBuildingId());
        if (building != null && building.getRepairUserId() != null) {
            order.setRepairUserId(building.getRepairUserId());
            order.setStatus(2);
            this.updateById(order);

            // 获取维修员信息
            SysUser repairUser = sysUserMapper.selectById(building.getRepairUserId());
            String repairUserName = repairUser != null ? repairUser.getRealName() : "维修员";

            // 发送系统消息给维修员和管理员
            String content = "学生 [" + studentName + "] 提交了新的报修单，地址：" + building.getBuildingName() + "，故障：" + order.getDescription();
            List<Long> userIds = new ArrayList<>();
            userIds.add(building.getRepairUserId()); // 维修员
            userIds.add(ADMIN_ID); // 管理员
            notificationService.sendSystemNotification(
                order.getId(), 1, "新的报修单", content, userIds
            );

            // 发送系统消息给学生（自动分配通知）
            String studentContent = "系统已为您的报修单 " + order.getOrderNo() + " 分配维修员 [" + repairUserName + "]";
            List<Long> studentUserIds = new ArrayList<>();
            studentUserIds.add(order.getStudentId());
            notificationService.sendSystemNotification(
                order.getId(), 1, "报修单已分配", studentContent, studentUserIds
            );

            // 记录自动分配操作
            RepairRecord record = new RepairRecord();
            record.setOrderId(order.getId());
            record.setRepairUserId(building.getRepairUserId());
            record.setAction(1);
            record.setRemark("系统自动分配");
            record.setCreateTime(LocalDateTime.now());
            repairRecordMapper.insert(record);
        } else {
            // 无对应维修员，通知管理员手动分配
            String content = "学生 [" + studentName + "] 提交了新的报修单，需要手动分配维修员";
            List<Long> userIds = new ArrayList<>();
            userIds.add(ADMIN_ID); // 管理员
            notificationService.sendSystemNotification(
                order.getId(), 1, "新的报修单", content, userIds
            );
        }

        return true;
    }

    /**
     * 维修员接受报修任务
     *
     * @param orderId 订单ID
     * @param repairUserId 维修员ID
     * @return 接单成功返回true，失败返回false（订单不存在或状态不正确）
     */
    @Override
    @Transactional
    public boolean acceptOrder(Long orderId, Long repairUserId) {
        // 验证订单状态是否为待接单(2)
        RepairOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 2) {
            return false;
        }
        
        order.setStatus(3); // 处理中
        order.setAcceptTime(LocalDateTime.now());
        if (!this.updateById(order)) {
            return false;
        }
        
        // 获取维修员信息用于消息通知
        SysUser repairUser = sysUserMapper.selectById(repairUserId);
        String repairUserName = repairUser != null ? repairUser.getRealName() : "维修员";
        
        // 发送系统消息给学生和管理员
        String content = "维修员 [" + repairUserName + "] 已接受您的报修单 " + order.getOrderNo();
        List<Long> userIds = new ArrayList<>();
        userIds.add(order.getStudentId()); // 学生
        userIds.add(ADMIN_ID); // 管理员
        notificationService.sendSystemNotification(
            orderId, 1, "维修员已接单", content, userIds
        );
        
        // 记录接单操作
        RepairRecord record = new RepairRecord();
        record.setOrderId(orderId);
        record.setRepairUserId(repairUserId);
        record.setAction(2); // 接受
        record.setRemark("维修员接受任务");
        record.setCreateTime(LocalDateTime.now());
        repairRecordMapper.insert(record);
        
        return true;
    }

    /**
     * 维修员拒绝接单
     * 需要填写拒单原因，订单状态变更为已拒绝
     *
     * @param orderId 订单ID
     * @param repairUserId 维修员ID
     * @param reason 拒单原因
     * @return 拒单成功返回true，失败返回false（订单不存在或状态不正确）
     */
    @Override
    @Transactional
    public boolean rejectOrder(Long orderId, Long repairUserId, String reason) {
        // 验证订单状态是否为待接单(2)或处理中(3)
        RepairOrder order = this.getById(orderId);
        if (order == null || (order.getStatus() != 2 && order.getStatus() != 3)) {
            return false;
        }

        order.setStatus(5); // 已拒绝
        order.setRejectReason(reason);
        if (!this.updateById(order)) {
            return false;
        }

        // 获取维修员信息用于消息通知
        SysUser repairUser = sysUserMapper.selectById(repairUserId);
        String repairUserName = repairUser != null ? repairUser.getRealName() : "维修员";
        
        // 发送系统消息给学生和管理员
        String content = "维修员 [" + repairUserName + "] 拒绝了您的报修单 " + order.getOrderNo() + "，原因：" + reason;
        List<Long> userIds = new ArrayList<>();
        userIds.add(order.getStudentId()); // 学生
        userIds.add(ADMIN_ID); // 管理员
        notificationService.sendSystemNotification(
            orderId, 1, "维修员拒单", content, userIds
        );

        // 记录拒单操作
        String remark = order.getStatus() == 2 ? "维修员拒单: " + reason : "维修失败: " + reason;
        RepairRecord record = new RepairRecord();
        record.setOrderId(orderId);
        record.setRepairUserId(repairUserId);
        record.setAction(3); // 拒绝
        record.setRemark(remark);
        record.setCreateTime(LocalDateTime.now());
        repairRecordMapper.insert(record);

        return true;
    }

    /**
     * 维修员完成维修
     * 填写维修结果和图片，订单状态变更为已完成
     *
     * @param orderId 订单ID
     * @param result 维修结果描述
     * @param images 维修图片URL（多个用逗号分隔）
     * @return 完成成功返回true，失败返回false（订单不存在或状态不正确）
     */
    @Override
    @Transactional
    public boolean completeOrder(Long orderId, String result, String images) {
        // 验证订单状态是否为处理中(3)
        RepairOrder order = this.getById(orderId);
        if (order == null || order.getStatus() != 3) {
            return false;
        }
        
        // 验证维修结果和维修图片都不能为空
        if (result == null || result.trim().isEmpty()) {
            return false;
        }
        if (images == null || images.trim().isEmpty()) {
            return false;
        }
        
        order.setStatus(4); // 已完成
        order.setRepairResult(result);
        order.setRepairImages(images);
        order.setCompleteTime(LocalDateTime.now());
        if (!this.updateById(order)) {
            return false;
        }
        
        // 发送系统消息给学生和管理员
        String content = "您的报修单 " + order.getOrderNo() + " 已完成维修，结果：" + result;
        List<Long> userIds = new ArrayList<>();
        userIds.add(order.getStudentId()); // 学生
        userIds.add(ADMIN_ID); // 管理员
        notificationService.sendSystemNotification(
            orderId, 1, "维修已完成", content, userIds
        );
        
        // 记录完成操作
        RepairRecord record = new RepairRecord();
        record.setOrderId(orderId);
        record.setRepairUserId(order.getRepairUserId());
        record.setAction(4); // 完成
        record.setRemark("维修完成: " + result);
        record.setCreateTime(LocalDateTime.now());
        repairRecordMapper.insert(record);
        
        return true;
    }

    /**
     * 学生取消报修单
     * 仅可取消待分配或待接单的报修单
     *
     * @param orderId 订单ID
     * @param studentId 学生ID（用于权限校验）
     * @return 取消成功返回true，失败返回false（订单不存在、状态不正确或无权取消）
     */
    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, Long studentId) {
        // 验证订单状态及权限
        RepairOrder order = this.getById(orderId);
        if (order == null || (order.getStatus() != 1 && order.getStatus() != 2)) {
            return false;
        }
        
        return doCancelOrder(order, studentId);
    }

    /**
     * 按订单编号取消报修单
     *
     * @param orderNo 订单编号
     * @param studentId 学生ID（用于权限校验）
     * @return 取消成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean cancelOrderByOrderNo(String orderNo, Long studentId) {
        // 根据订单编号查询订单
        RepairOrder order = this.getOne(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getOrderNo, orderNo));
        
        if (order == null || (order.getStatus() != 1 && order.getStatus() != 2)) {
            return false;
        }
        
        return doCancelOrder(order, studentId);
    }

    /**
     * 执行取消订单操作
     *
     * @param order 订单对象
     * @param studentId 学生ID
     * @return 取消成功返回true
     */
    private boolean doCancelOrder(RepairOrder order, Long studentId) {
        if (!order.getStudentId().equals(studentId)) {
            return false;
        }
        
        order.setStatus(0); // 已取消
        if (!this.updateById(order)) {
            return false;
        }
        
        // 获取学生信息用于消息通知
        SysUser student = sysUserMapper.selectById(studentId);
        String studentName = student != null ? student.getRealName() : "学生";
        
        // 发送系统消息给维修员和管理员
        List<Long> userIds = new ArrayList<>();
        if (order.getRepairUserId() != null) {
            userIds.add(order.getRepairUserId()); // 维修员
        }
        userIds.add(ADMIN_ID); // 管理员
        
        String content = "学生 [" + studentName + "] 取消了报修单 " + order.getOrderNo();
        notificationService.sendSystemNotification(
            order.getId(), 1, "报修单已取消", content, userIds
        );
        
        return true;
    }

    /**
     * 管理员手动分配维修任务
     * 支持重新分配被拒绝的订单
     *
     * @param orderId 订单ID
     * @param repairUserId 维修员ID
     * @return 分配成功返回true，失败返回false（订单不存在或状态不正确）
     */
    @Override
    @Transactional
    public boolean assignOrder(Long orderId, Long repairUserId) {
        // 验证订单状态是否为待分配(1)或已拒绝(5)
        RepairOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }

        if (order.getStatus() != 1 && order.getStatus() != 5) {
            return false;
        }

        boolean isReassign = order.getStatus() == 5;

        // 使用 LambdaUpdateWrapper 执行更新操作
        LambdaUpdateWrapper<RepairOrder> updateWrapper = new LambdaUpdateWrapper<RepairOrder>()
                .eq(RepairOrder::getId, orderId)
                .set(RepairOrder::getRepairUserId, repairUserId)
                .set(RepairOrder::getStatus, 2);

        if (isReassign) {
            updateWrapper.set(RepairOrder::getRejectReason, null); // 只有状态为5时才清除拒绝理由
        }

        boolean updated = this.update(updateWrapper);
        if (!updated) {
            return false;
        }

        // 获取维修员信息用于消息通知
        SysUser repairUser = sysUserMapper.selectById(repairUserId);
        String repairUserName = repairUser != null ? repairUser.getRealName() : "维修员";
        
        // 发送系统消息给维修员和学生
        List<Long> userIds = new ArrayList<>();
        userIds.add(repairUserId); // 维修员
        userIds.add(order.getStudentId()); // 学生
        
        String content = "您已被分配新的报修单 " + order.getOrderNo() + "，请及时处理";
        notificationService.sendSystemNotification(
            orderId, 1, "报修单已分配", content, userIds
        );

        // 记录分配操作
        String remark = isReassign ? "重新分配" : "管理员手动分配";
        RepairRecord record = new RepairRecord();
        record.setOrderId(orderId);
        record.setRepairUserId(repairUserId);
        record.setAction(1); // 分配
        record.setRemark(remark);
        record.setCreateTime(LocalDateTime.now());
        repairRecordMapper.insert(record);

        return true;
    }

    /**
     * 根据筛选条件搜索报修单
     *
     * @param status 报修状态（可选）
     * @param buildingId 楼栋ID（可选）
     * @param faultTypeId 故障类型ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param keyword 搜索关键词（可选，匹配故障描述和维修结果）
     * @return 符合条件的报修单列表
     */
    @Override
    public List<RepairOrder> searchRepairOrders(Integer status, Long buildingId, Long faultTypeId,
                                                LocalDate startDate, LocalDate endDate, String keyword) {
        // 构建动态查询条件
        LambdaQueryWrapper<RepairOrder> queryWrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (status != null) {
            queryWrapper.eq(RepairOrder::getStatus, status);
        }
        
        // 楼栋筛选
        if (buildingId != null) {
            queryWrapper.eq(RepairOrder::getBuildingId, buildingId);
        }
        
        // 故障类型筛选
        if (faultTypeId != null) {
            queryWrapper.eq(RepairOrder::getFaultTypeId, faultTypeId);
        }
        
        // 日期范围筛选
        if (startDate != null) {
            queryWrapper.ge(RepairOrder::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            queryWrapper.lt(RepairOrder::getCreateTime, endDate.plusDays(1).atStartOfDay());
        }
        
        // 关键字搜索（订单编号、故障描述和维修结果）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like(RepairOrder::getOrderNo, keyword)
                .or()
                .like(RepairOrder::getDescription, keyword)
                .or()
                .like(RepairOrder::getRepairResult, keyword)
            );
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(RepairOrder::getCreateTime);

        return this.list(queryWrapper);
    }

    /**
     * 学生更新报修单
     * 仅可更新待分配或待接单状态的报修单
     *
     * @param orderId 订单ID
     * @param faultTypeId 故障类型ID
     * @param description 故障描述
     * @param images 故障图片
     * @param studentId 学生ID（用于权限校验）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean updateOrder(Long orderId, Long faultTypeId, String description, String images, Long studentId) {
        // 验证订单状态及权限
        RepairOrder order = this.getById(orderId);
        if (order == null || (order.getStatus() != 1 && order.getStatus() != 2)) {
            return false;
        }
        
        if (!order.getStudentId().equals(studentId)) {
            return false;
        }
        
        order.setFaultTypeId(faultTypeId);
        order.setDescription(description);
        order.setImages(images);
        order.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(order);
    }

    /**
     * 根据用户角色查询对应的报修单列表
     *
     * @param userId 用户ID
     * @param role 用户角色（1-学生，2-维修员，3-管理员）
     * @return 报修单列表
     */
    @Override
    public List<RepairOrder> listOrdersByRole(Long userId, Integer role) {
        // 根据角色过滤数据
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(RepairOrder::getCreateTime);

        if (role == 1) {
            wrapper.eq(RepairOrder::getStudentId, userId);
        } else if (role == 2) {
            wrapper.eq(RepairOrder::getRepairUserId, userId);
        }

        return this.list(wrapper);
    }

    /**
     * 分页查询报修单列表并组装详细信息
     * 根据用户角色进行权限过滤
     *
     * @param status 报修状态（可选）
     * @param buildingId 楼栋ID（可选）
     * @param faultTypeId 故障类型ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param keyword 搜索关键词（可选）
     * @param userId 用户ID
     * @param role 用户角色（1-学生，2-维修员，3-管理员）
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public IPage<Map<String, Object>> listOrdersWithDetailsPage(Integer status, Long buildingId, Long faultTypeId,
                                                                 LocalDate startDate, LocalDate endDate, String keyword,
                                                                 Long userId, Integer role, long current, long size) {
        // 先查询基础数据用于过滤
        List<RepairOrder> orders = searchRepairOrders(status, buildingId, faultTypeId, startDate, endDate, keyword);

        // 根据角色过滤数据
        List<RepairOrder> filteredOrders = new ArrayList<>();
        for (RepairOrder order : orders) {
            if (role == 1) {
                if (order.getStudentId().equals(userId)) {
                    filteredOrders.add(order);
                }
            } else if (role == 2) {
                // 维修员可以看到：
                // 1. 待接单状态（status=2）的订单（可能已分配给这个维修员或未分配）
                // 2. 分配给自己的所有订单（状态不限）
                if (order.getStatus() == 2) {
                    // 状态为2的待接单订单，如果未分配维修员或者分配给了自己
                    if (order.getRepairUserId() == null || order.getRepairUserId().equals(userId)) {
                        filteredOrders.add(order);
                    }
                } else if (order.getRepairUserId() != null && order.getRepairUserId().equals(userId)) {
                    // 其他状态的订单，只要分配给自己
                    filteredOrders.add(order);
                }
            } else {
                filteredOrders.add(order);
            }
        }

        // 构建详细信息
        List<Map<String, Object>> allDetails = new ArrayList<>();
        for (RepairOrder order : filteredOrders) {
            allDetails.add(buildOrderDetail(order));
        }

        // 实现内存分页
        long total = allDetails.size();
        long fromIndex = (current - 1) * size;
        long toIndex = Math.min(fromIndex + size, total);

        List<Map<String, Object>> pageRecords;
        if (fromIndex >= total) {
            pageRecords = new ArrayList<>();
        } else {
            pageRecords = allDetails.subList((int) fromIndex, (int) toIndex);
        }

        // 构建分页结果
        Page<Map<String, Object>> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(pageRecords);

        return page;
    }

    /**
     * 查询报修单列表并组装详细信息
     * 根据用户角色进行权限过滤
     *
     * @param status 报修状态（可选）
     * @param buildingId 楼栋ID（可选）
     * @param faultTypeId 故障类型ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param keyword 搜索关键词（可选）
     * @param userId 用户ID
     * @param role 用户角色（1-学生，2-维修员，3-管理员）
     * @return 包含详细信息的报修单列表
     */
    @Override
    public List<Map<String, Object>> listOrdersWithDetails(Integer status, Long buildingId, Long faultTypeId,
                                                            LocalDate startDate, LocalDate endDate, String keyword,
                                                            Long userId, Integer role) {
        // 查询基础报修单数据
        List<RepairOrder> orders = searchRepairOrders(status, buildingId, faultTypeId, startDate, endDate, keyword);

        // 根据角色过滤数据
        List<RepairOrder> filteredOrders = new ArrayList<>();
        for (RepairOrder order : orders) {
            if (role == 1) {
                if (order.getStudentId().equals(userId)) {
                    filteredOrders.add(order);
                }
            } else if (role == 2) {
                // 维修员可以看到：
                // 1. 待接单状态（status=2）的订单（可能已分配给这个维修员或未分配）
                // 2. 分配给自己的所有订单（状态不限）
                if (order.getStatus() == 2) {
                    // 状态为2的待接单订单，如果未分配维修员或者分配给了自己
                    if (order.getRepairUserId() == null || order.getRepairUserId().equals(userId)) {
                        filteredOrders.add(order);
                    }
                } else if (order.getRepairUserId() != null && order.getRepairUserId().equals(userId)) {
                    // 其他状态的订单，只要分配给自己
                    filteredOrders.add(order);
                }
            } else {
                filteredOrders.add(order);
            }
        }

        // 组装详细信息
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (RepairOrder order : filteredOrders) {
            resultList.add(buildOrderDetail(order));
        }

        return resultList;
    }

    /**
     * 获取单个报修单的详细信息
     *
     * @param orderId 订单ID
     * @return 包含完整信息的报修单详情Map
     * @throws IllegalArgumentException 当报修单不存在时抛出
     */
    @Override
    public Map<String, Object> getRepairOrderDetails(Long orderId) {
        RepairOrder order = this.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("报修单不存在");
        }
        return buildOrderDetail(order);
    }

    /**
     * 组装报修单详细信息
     * 包含楼栋、房间、学生、维修员等关联信息
     *
     * @param order 报修单对象
     * @return 包含完整信息的报修单详情Map
     */
    private Map<String, Object> buildOrderDetail(RepairOrder order) {
        // 基础信息
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

        // 关联查询楼栋信息
        if (order.getBuildingId() != null) {
            Building building = buildingMapper.selectById(order.getBuildingId());
            if (building != null) {
                orderDetail.put("buildingName", building.getBuildingName());
            }
        }

        // 关联查询房间号
        String roomNumberStr = "";
        if (order.getRoomId() != null) {
            Room room = roomService.getById(order.getRoomId());
            if (room != null) {
                roomNumberStr = room.getRoomNumber();
                orderDetail.put("roomNumber", room.getRoomNumber());
            }
        }

        // 构建完整的宿舍地址
        if (orderDetail.get("buildingName") != null && !roomNumberStr.isEmpty()) {
            orderDetail.put("dormitoryName", orderDetail.get("buildingName") + " " + roomNumberStr);
        } else if (orderDetail.get("buildingName") != null) {
            orderDetail.put("dormitoryName", orderDetail.get("buildingName"));
        } else if (!roomNumberStr.isEmpty()) {
            orderDetail.put("dormitoryName", roomNumberStr);
        }

        // 关联查询故障类型信息
        if (order.getFaultTypeId() != null) {
            FaultType faultType = faultTypeMapper.selectById(order.getFaultTypeId());
            if (faultType != null) {
                orderDetail.put("faultTypeName", faultType.getTypeName());
            }
        }

        // 关联查询学生信息
        if (order.getStudentId() != null) {
            SysUser student = sysUserMapper.selectById(order.getStudentId());
            if (student != null) {
                orderDetail.put("studentName", student.getRealName());
                orderDetail.put("studentPhone", student.getPhone());
                orderDetail.put("studentClass", student.getClassName());
            }
        }

        // 关联查询维修员信息
        if (order.getRepairUserId() != null) {
            SysUser repairUser = sysUserMapper.selectById(order.getRepairUserId());
            if (repairUser != null) {
                orderDetail.put("repairUserName", repairUser.getRealName());
                orderDetail.put("repairUserPhone", repairUser.getPhone());
            }
        }

        return orderDetail;
    }

}
