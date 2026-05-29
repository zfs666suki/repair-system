package com.pxxy.houduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pxxy.houduan.entity.RepairRecord;

import java.util.List;

public interface RepairRecordService extends IService<RepairRecord> {

    /**
     * 根据订单ID查询维修记录
     * @param orderId 订单ID
     * @return 维修记录列表
     */
    List<RepairRecord> getRecordsByOrderId(Long orderId);

    /**
     * 查询某个维修员的所有维修记录
     * @param repairUserId 维修员ID
     * @return 维修记录列表
     */
    List<RepairRecord> getRecordsByRepairUserId(Long repairUserId);
}
