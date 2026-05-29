package com.pxxy.houduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pxxy.houduan.entity.RepairOrder;
import com.pxxy.houduan.service.RepairOrderService;
import com.pxxy.houduan.service.StatisticsService;
import com.pxxy.houduan.service.BuildingService;
import com.pxxy.houduan.service.FaultTypeService;
import com.pxxy.houduan.service.SysUserService;
import com.pxxy.houduan.entity.Building;
import com.pxxy.houduan.entity.FaultType;
import com.pxxy.houduan.entity.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 * 提供报修单的多维度统计分析功能，包括按日期、楼栋、故障类型、状态等维度的统计
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    private final RepairOrderService repairOrderService;
    private final BuildingService buildingService;
    private final FaultTypeService faultTypeService;
    private final SysUserService sysUserService;

    public StatisticsServiceImpl(RepairOrderService repairOrderService, BuildingService buildingService, FaultTypeService faultTypeService, SysUserService sysUserService) {
        this.repairOrderService = repairOrderService;
        this.buildingService = buildingService;
        this.faultTypeService = faultTypeService;
        this.sysUserService = sysUserService;
    }

    /**
     * 按日期统计报修单数量
     *
     * @param startDate 开始日期（包含）
     * @param endDate 结束日期（包含）
     * @return 统计数据列表，每个元素包含 date 和 count 属性
     */
    @Override
    public List<Map<String, Object>> statisticsByDate(LocalDate startDate, LocalDate endDate) {
        logger.info("开始时间{}", startDate);
        logger.info("结束时间{}", endDate);
        // 查询指定时间范围内的报修单
        LambdaQueryWrapper<RepairOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(RepairOrder::getCreateTime, LocalDateTime.of(startDate, LocalTime.MIN));
        queryWrapper.lt(RepairOrder::getCreateTime, LocalDateTime.of(endDate.plusDays(1), LocalTime.MIN));

        List<RepairOrder> orders = repairOrderService.list(queryWrapper);
        Map<String, Integer> countMap = new HashMap<>();

        // 按日期分组统计数量
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (RepairOrder order : orders) {
            String dateStr = order.getCreateTime().format(formatter);
            countMap.put(dateStr, countMap.getOrDefault(dateStr, 0) + 1);
        }

        // 转换为列表格式并按日期排序
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }

        // 按日期升序排序
        result.sort((a, b) -> {
            String dateA = (String) a.get("date");
            String dateB = (String) b.get("date");
            return dateA.compareTo(dateB);
        });

        return result;
    }

    /**
     * 按楼栋统计报修单数量
     *
     * @param startDate 开始日期（包含）
     * @param endDate 结束日期（包含）
     * @return 统计数据列表，每个元素包含 buildingName 和 count 属性
     */
    @Override
    public List<Map<String, Object>> statisticsByBuilding(LocalDate startDate, LocalDate endDate) {
        // 查询指定时间范围内的报修单
        LambdaQueryWrapper<RepairOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(RepairOrder::getCreateTime, LocalDateTime.of(startDate, LocalTime.MIN));
        queryWrapper.lt(RepairOrder::getCreateTime, LocalDateTime.of(endDate.plusDays(1), LocalTime.MIN));

        List<RepairOrder> orders = repairOrderService.list(queryWrapper);
        Map<Long, Integer> buildingCountMap = new HashMap<>();

        // 按楼栋ID分组统计
        for (RepairOrder order : orders) {
            if (order.getBuildingId() != null) {
                buildingCountMap.put(order.getBuildingId(), buildingCountMap.getOrDefault(order.getBuildingId(), 0) + 1);
            }
        }

        // 将楼栋ID转换为楼栋名称并生成列表
        List<Map<String, Object>> result = new ArrayList<>();
        List<Building> buildings = buildingService.list();
        Map<Long, String> buildingNameMap = buildings.stream()
                .collect(Collectors.toMap(Building::getId, Building::getBuildingName));

        for (Map.Entry<Long, Integer> entry : buildingCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("buildingName", buildingNameMap.getOrDefault(entry.getKey(), "未知楼栋"));
            item.put("count", entry.getValue());
            result.add(item);
        }

        return result;
    }

    /**
     * 按故障类型统计报修单数量
     *
     * @param startDate 开始日期（包含）
     * @param endDate 结束日期（包含）
     * @return 统计数据列表，每个元素包含 typeName 和 count 属性
     */
    @Override
    public List<Map<String, Object>> statisticsByFaultType(LocalDate startDate, LocalDate endDate) {
        // 查询指定时间范围内的报修单
        LambdaQueryWrapper<RepairOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(RepairOrder::getCreateTime, LocalDateTime.of(startDate, LocalTime.MIN));
        queryWrapper.lt(RepairOrder::getCreateTime, LocalDateTime.of(endDate.plusDays(1), LocalTime.MIN));

        List<RepairOrder> orders = repairOrderService.list(queryWrapper);
        Map<Long, Integer> faultTypeCountMap = new HashMap<>();

        // 按故障类型ID分组统计
        for (RepairOrder order : orders) {
            if (order.getFaultTypeId() != null) {
                faultTypeCountMap.put(order.getFaultTypeId(), faultTypeCountMap.getOrDefault(order.getFaultTypeId(), 0) + 1);
            }
        }

        // 将故障类型ID转换为故障类型名称并生成列表
        List<Map<String, Object>> result = new ArrayList<>();
        List<FaultType> faultTypes = faultTypeService.list();
        Map<Long, String> faultTypeNameMap = faultTypes.stream()
                .collect(Collectors.toMap(FaultType::getId, FaultType::getTypeName));

        for (Map.Entry<Long, Integer> entry : faultTypeCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("typeName", faultTypeNameMap.getOrDefault(entry.getKey(), "未知类型"));
            item.put("count", entry.getValue());
            result.add(item);
        }

        return result;
    }

    /**
     * 按报修状态统计报修单数量
     *
     * @param startDate 开始日期（包含）
     * @param endDate 结束日期（包含）
     * @return 统计数据列表，每个元素包含 status 和 count 属性
     */
    @Override
    public List<Map<String, Object>> statisticsByStatus(LocalDate startDate, LocalDate endDate) {
        // 查询指定时间范围内的报修单
        LambdaQueryWrapper<RepairOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(RepairOrder::getCreateTime, LocalDateTime.of(startDate, LocalTime.MIN));
        queryWrapper.lt(RepairOrder::getCreateTime, LocalDateTime.of(endDate.plusDays(1), LocalTime.MIN));

        List<RepairOrder> orders = repairOrderService.list(queryWrapper);
        Map<Integer, Integer> countMap = new HashMap<>();

        // 按状态分组统计
        for (RepairOrder order : orders) {
            countMap.put(order.getStatus(), countMap.getOrDefault(order.getStatus(), 0) + 1);
        }

        // 转换为列表格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }

        return result;
    }

    /**
     * 获取报修统计汇总信息
     * 包含总数、各状态数量及多维度统计数据
     *
     * @param startDate 开始日期（包含）
     * @param endDate 结束日期（包含）
     * @return 包含完整统计信息的Map
     */
    @Override
    public Map<String, Object> getStatisticsSummary(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();

        // 统计总报修数
        LambdaQueryWrapper<RepairOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(RepairOrder::getCreateTime, LocalDateTime.of(startDate, LocalTime.MIN));
        queryWrapper.lt(RepairOrder::getCreateTime, LocalDateTime.of(endDate.plusDays(1), LocalTime.MIN));
        long totalCount = repairOrderService.count(queryWrapper);
        summary.put("totalCount", totalCount);

        // 统计已完成数
        LambdaQueryWrapper<RepairOrder> completedQueryWrapper = new LambdaQueryWrapper<>();
        completedQueryWrapper.ge(RepairOrder::getCreateTime, startDate.atStartOfDay());
        completedQueryWrapper.lt(RepairOrder::getCreateTime, endDate.plusDays(1).atStartOfDay());
        completedQueryWrapper.eq(RepairOrder::getStatus, 4);
        long completedCount = repairOrderService.count(completedQueryWrapper);
        summary.put("completedCount", completedCount);

        // 统计待处理数
        LambdaQueryWrapper<RepairOrder> pendingQueryWrapper = new LambdaQueryWrapper<>();
        pendingQueryWrapper.ge(RepairOrder::getCreateTime, startDate.atStartOfDay());
        pendingQueryWrapper.lt(RepairOrder::getCreateTime, endDate.plusDays(1).atStartOfDay());
        pendingQueryWrapper.eq(RepairOrder::getStatus, 1);
        long pendingCount = repairOrderService.count(pendingQueryWrapper);
        summary.put("pendingCount", pendingCount);

        // 统计处理中数
        LambdaQueryWrapper<RepairOrder> processingQueryWrapper = new LambdaQueryWrapper<>();
        processingQueryWrapper.ge(RepairOrder::getCreateTime, startDate.atStartOfDay());
        processingQueryWrapper.lt(RepairOrder::getCreateTime, endDate.plusDays(1).atStartOfDay());
        processingQueryWrapper.eq(RepairOrder::getStatus, 2);
        long processingCount = repairOrderService.count(processingQueryWrapper);
        summary.put("processingCount", processingCount);

        // 统计已拒绝数
        LambdaQueryWrapper<RepairOrder> rejectedQueryWrapper = new LambdaQueryWrapper<>();
        rejectedQueryWrapper.ge(RepairOrder::getCreateTime, startDate.atStartOfDay());
        rejectedQueryWrapper.lt(RepairOrder::getCreateTime, endDate.plusDays(1).atStartOfDay());
        rejectedQueryWrapper.eq(RepairOrder::getStatus, 5);
        long rejectedCount = repairOrderService.count(rejectedQueryWrapper);
        summary.put("rejectedCount", rejectedCount);

        // 组装多维度统计数据
        summary.put("byDate", statisticsByDate(startDate, endDate));
        summary.put("byBuilding", statisticsByBuilding(startDate, endDate));
        summary.put("byFaultType", statisticsByFaultType(startDate, endDate));
        summary.put("byStatus", statisticsByStatus(startDate, endDate));

        return summary;
    }

    /**
     * 将报修状态码转换为中文描述
     *
     * @param status 状态码
     * @return 状态中文名称
     */
    private String getStatusName(Integer status) {
        switch (status) {
            case 1:
                return "待处理";
            case 2:
                return "处理中";
            case 3:
                return "待验收";
            case 4:
                return "已完成";
            case 5:
                return "已拒绝";
            case 6:
                return "已取消";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取指定维修员在特定时间段内的统计信息
     * 包含接单数、完成数、完成率及平均维修时长
     *
     * @param repairUserId 维修员ID
     * @param startDate 开始日期（包含）
     * @param endDate 结束日期（包含）
     * @return 包含维修员统计数据的Map
     */
    @Override
    public Map<String, Object> getRepairUserStatistics(Long repairUserId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 查询该维修员在指定时间范围内的所有报修单
        LambdaQueryWrapper<RepairOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(RepairOrder::getCreateTime, LocalDateTime.of(startDate, LocalTime.MIN));
        queryWrapper.lt(RepairOrder::getCreateTime, LocalDateTime.of(endDate.plusDays(1), LocalTime.MIN));
        queryWrapper.eq(RepairOrder::getRepairUserId, repairUserId);
        
        List<RepairOrder> orders = repairOrderService.list(queryWrapper);
        
        // 统计各状态的报修单数量
        long totalCount = orders.size();
        long completedCount = orders.stream().filter(o -> o.getStatus() == 4).count();
        long pendingCount = orders.stream().filter(o -> o.getStatus() == 2).count();
        long inProgressCount = orders.stream().filter(o -> o.getStatus() == 3).count();
        long rejectedCount = orders.stream().filter(o -> o.getStatus() == 5).count();
        
        // 计算平均维修时长（仅统计已完成的订单）
        double avgRepairTime = 0;
        int completedWithTimeCount = 0;
        long totalRepairMinutes = 0;
        
        for (RepairOrder order : orders) {
            if (order.getStatus() == 4 && order.getAcceptTime() != null && order.getCompleteTime() != null) {
                Duration duration = Duration.between(order.getAcceptTime(), order.getCompleteTime());
                totalRepairMinutes += duration.toMinutes();
                completedWithTimeCount++;
            }
        }
        
        if (completedWithTimeCount > 0) {
            avgRepairTime = (double) totalRepairMinutes / completedWithTimeCount;
        }
        
        statistics.put("repairUserId", repairUserId);
        statistics.put("orderCount", totalCount);
        statistics.put("completedCount", completedCount);
        statistics.put("pendingCount", pendingCount);
        statistics.put("inProgressCount", inProgressCount);
        statistics.put("rejectedCount", rejectedCount);
        statistics.put("avgRepairMinutes", avgRepairTime);
        
        // 计算完成率（返回0-1之间的小数）
        if (totalCount > 0) {
            double completionRate = (double) completedCount / totalCount;
            statistics.put("completionRate", completionRate);
        } else {
            statistics.put("completionRate", 0.0);
        }
        
        return statistics;
    }

    /**
     * 获取所有维修员在特定时间段内的统计列表
     *
     * @param startDate 开始日期（包含）
     * @param endDate 结束日期（包含）
     * @return 包含所有维修员统计数据的列表
     */
    @Override
    public List<Map<String, Object>> getAllRepairUserStatistics(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        
        // 查询所有角色为维修员(2)的用户
        List<SysUser> repairUsers = sysUserService.lambdaQuery()
                .eq(SysUser::getRole, 2)
                .list();
        
        // 逐个获取每个维修员的统计数据并补充基本信息
        for (SysUser repairUser : repairUsers) {
            Map<String, Object> userStatistics = getRepairUserStatistics(repairUser.getId(), startDate, endDate);
            userStatistics.put("repairUserId", repairUser.getId());
            userStatistics.put("realName", repairUser.getRealName());
            userStatistics.put("phone", repairUser.getPhone());
            resultList.add(userStatistics);
        }
        
        return resultList;
    }

}
