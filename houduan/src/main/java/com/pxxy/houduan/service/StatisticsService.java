package com.pxxy.houduan.service;

import java.util.Map;
import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

    /**
     * 按时间统计报修数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 按日期统计的报修数量列表，每个元素包含 date 和 count
     */
    List<Map<String, Object>> statisticsByDate(LocalDate startDate, LocalDate endDate);

    /**
     * 按楼栋统计报修数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 按楼栋统计的报修数量列表，每个元素包含 buildingName 和 count
     */
    List<Map<String, Object>> statisticsByBuilding(LocalDate startDate, LocalDate endDate);

    /**
     * 按故障类型统计报修数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 按故障类型统计的报修数量列表，每个元素包含 typeName 和 count
     */
    List<Map<String, Object>> statisticsByFaultType(LocalDate startDate, LocalDate endDate);

    /**
     * 按状态统计报修数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 按状态统计的报修数量列表，每个元素包含 status 和 count
     */
    List<Map<String, Object>> statisticsByStatus(LocalDate startDate, LocalDate endDate);

    /**
     * 获取报修统计汇总
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计汇总信息
     */
    Map<String, Object> getStatisticsSummary(LocalDate startDate, LocalDate endDate);

    /**
     * 获取维修员统计信息
     * @param repairUserId 维修员ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 维修员统计信息
     */
    Map<String, Object> getRepairUserStatistics(Long repairUserId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取所有维修员的统计列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 维修员统计列表
     */
    List<Map<String, Object>> getAllRepairUserStatistics(LocalDate startDate, LocalDate endDate);

}
