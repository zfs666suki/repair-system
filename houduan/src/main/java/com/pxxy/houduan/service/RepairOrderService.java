package com.pxxy.houduan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pxxy.houduan.entity.RepairOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface RepairOrderService extends IService<RepairOrder> {

    boolean submitOrder(RepairOrder order);

    boolean acceptOrder(Long orderId, Long repairUserId);

    boolean rejectOrder(Long orderId, Long repairUserId, String reason);

    boolean completeOrder(Long orderId, String result, String images);

    boolean cancelOrder(Long orderId, Long studentId);

    boolean cancelOrderByOrderNo(String orderNo, Long studentId);

    boolean assignOrder(Long orderId, Long repairUserId);

    boolean updateOrder(Long orderId, Long faultTypeId, String description, String images, Long studentId);

    /**
     * 搜索和筛选报修单
     * @param status 状态筛选，可选
     * @param buildingId 楼栋ID筛选，可选
     * @param faultTypeId 故障类型ID筛选，可选
     * @param startDate 开始日期，可选
     * @param endDate 结束日期，可选
     * @param keyword 关键字搜索，可选
     * @return 筛选后的报修单列表
     */
    List<RepairOrder> searchRepairOrders(Integer status, Long buildingId, Long faultTypeId,
                                        LocalDate startDate, LocalDate endDate, String keyword);

    List<RepairOrder> listOrdersByRole(Long userId, Integer role);

    IPage<Map<String, Object>> listOrdersWithDetailsPage(Integer status, Long buildingId, Long faultTypeId,
                                                          LocalDate startDate, LocalDate endDate, String keyword,
                                                          Long userId, Integer role, long current, long size);

    List<Map<String, Object>> listOrdersWithDetails(Integer status, Long buildingId, Long faultTypeId,
                                                     LocalDate startDate, LocalDate endDate, String keyword,
                                                     Long userId, Integer role);

    Map<String, Object> getRepairOrderDetails(Long orderId);

}
