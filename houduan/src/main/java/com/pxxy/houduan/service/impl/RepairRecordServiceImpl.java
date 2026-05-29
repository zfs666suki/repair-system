package com.pxxy.houduan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pxxy.houduan.entity.RepairRecord;
import com.pxxy.houduan.mapper.RepairRecordMapper;
import com.pxxy.houduan.service.RepairRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 维修记录服务实现类
 * 提供维修记录的查询功能
 */
@Service
public class RepairRecordServiceImpl extends ServiceImpl<RepairRecordMapper, RepairRecord> implements RepairRecordService {

    /**
     * 根据订单ID查询该报修单的所有维修记录
     *
     * @param orderId 订单ID
     * @return 维修记录列表，按创建时间降序排列
     */
    @Override
    public List<RepairRecord> getRecordsByOrderId(Long orderId) {
        return this.lambdaQuery()
                .eq(RepairRecord::getOrderId, orderId)
                .orderByDesc(RepairRecord::getCreateTime)
                .list();
    }

    /**
     * 根据维修员ID查询其所有维修记录
     *
     * @param repairUserId 维修员ID
     * @return 维修记录列表，按创建时间降序排列
     */
    @Override
    public List<RepairRecord> getRecordsByRepairUserId(Long repairUserId) {
        return this.lambdaQuery()
                .eq(RepairRecord::getRepairUserId, repairUserId)
                .orderByDesc(RepairRecord::getCreateTime)
                .list();
    }
}
