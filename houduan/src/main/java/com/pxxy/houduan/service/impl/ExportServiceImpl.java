package com.pxxy.houduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pxxy.houduan.entity.RepairOrder;
import com.pxxy.houduan.service.RepairOrderService;
import com.pxxy.houduan.service.ExportService;
import com.pxxy.houduan.service.SysUserService;
import com.pxxy.houduan.service.BuildingService;
import com.pxxy.houduan.service.FaultTypeService;
import com.pxxy.houduan.entity.SysUser;
import com.pxxy.houduan.entity.Building;
import com.pxxy.houduan.entity.FaultType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导出服务实现类
 * 提供报修单Excel导出功能，包含数据查询、格式化和文件生成
 */
@Service
public class ExportServiceImpl implements ExportService {

    private final RepairOrderService repairOrderService;
    private final SysUserService sysUserService;
    private final BuildingService buildingService;
    private final FaultTypeService faultTypeService;

    public ExportServiceImpl(RepairOrderService repairOrderService, SysUserService sysUserService, BuildingService buildingService, FaultTypeService faultTypeService) {
        this.repairOrderService = repairOrderService;
        this.sysUserService = sysUserService;
        this.buildingService = buildingService;
        this.faultTypeService = faultTypeService;
    }

    /**
     * 导出指定时间范围内的报修单为Excel工作簿
     *
     * @param startDate 开始日期（包含）
     * @param endDate 结束日期（包含）
     * @return 包含报修单数据的Excel工作簿对象
     */
    @Override
    public Workbook exportRepairOrders(LocalDate startDate, LocalDate endDate) {
        // 查询指定时间范围内的报修单数据
        LambdaQueryWrapper<RepairOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(RepairOrder::getCreateTime, startDate.atStartOfDay());
        queryWrapper.lt(RepairOrder::getCreateTime, endDate.plusDays(1).atStartOfDay());
        queryWrapper.orderByDesc(RepairOrder::getCreateTime);
        List<RepairOrder> orders = repairOrderService.list(queryWrapper);

        // 创建Excel工作簿和工作表
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("报修单记录");

        // 设置各列宽度以适配内容显示
        sheet.setColumnWidth(0, 15 * 256);
        sheet.setColumnWidth(1, 20 * 256);
        sheet.setColumnWidth(2, 15 * 256);
        sheet.setColumnWidth(3, 15 * 256);
        sheet.setColumnWidth(4, 15 * 256);
        sheet.setColumnWidth(5, 20 * 256);
        sheet.setColumnWidth(6, 30 * 256);
        sheet.setColumnWidth(7, 15 * 256);
        sheet.setColumnWidth(8, 15 * 256);
        sheet.setColumnWidth(9, 15 * 256);
        sheet.setColumnWidth(10, 20 * 256);
        sheet.setColumnWidth(11, 30 * 256);
        sheet.setColumnWidth(12, 15 * 256);

        // 创建标题行并定义列名
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "订单号", "学生姓名", "楼栋", "房间", "故障类型", 
                "报修时间", "故障描述", "维修人员", "状态", 
                "受理时间", "完成时间", "维修结果", "拒绝原因"
        };

        // 创建标题行样式：加粗、居中、浅蓝色背景
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 填充标题行单元格
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 预加载基础数据映射，避免循环中多次查询数据库
        Map<Long, String> userMap = sysUserService.list().stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername));
        Map<Long, String> buildingMap = buildingService.list().stream()
                .collect(Collectors.toMap(Building::getId, Building::getBuildingName));
        Map<Long, String> faultTypeMap = faultTypeService.list().stream()
                .collect(Collectors.toMap(FaultType::getId, FaultType::getTypeName));

        // 遍历报修单列表，逐行填充数据到Excel
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < orders.size(); i++) {
            RepairOrder order = orders.get(i);
            Row dataRow = sheet.createRow(i + 1);

            dataRow.createCell(0).setCellValue(order.getOrderNo());
            dataRow.createCell(1).setCellValue(userMap.getOrDefault(order.getStudentId(), "未知"));
            dataRow.createCell(2).setCellValue(buildingMap.getOrDefault(order.getBuildingId(), "未知"));
            dataRow.createCell(3).setCellValue(order.getRoomId() != null ? order.getRoomId().toString() : "未知");
            dataRow.createCell(4).setCellValue(faultTypeMap.getOrDefault(order.getFaultTypeId(), "未知"));
            dataRow.createCell(5).setCellValue(order.getCreateTime().format(formatter));
            dataRow.createCell(6).setCellValue(order.getDescription());
            dataRow.createCell(7).setCellValue(userMap.getOrDefault(order.getRepairUserId(), "未分配"));
            dataRow.createCell(8).setCellValue(getStatusName(order.getStatus()));
            dataRow.createCell(9).setCellValue(order.getAcceptTime() != null ? order.getAcceptTime().format(formatter) : "");
            dataRow.createCell(10).setCellValue(order.getCompleteTime() != null ? order.getCompleteTime().format(formatter) : "");
            dataRow.createCell(11).setCellValue(order.getRepairResult() != null ? order.getRepairResult() : "");
            dataRow.createCell(12).setCellValue(order.getRejectReason() != null ? order.getRejectReason() : "");
        }

        return workbook;
    }

    /**
     * 将报修状态码转换为中文描述
     *
     * @param status 状态码（0-已取消，1-待分配，2-待接单，3-处理中，4-已完成，5-已拒绝）
     * @return 状态中文名称
     */
    private String getStatusName(Integer status) {
        switch (status) {
            case 0:
                return "已取消";
            case 1:
                return "待分配";
            case 2:
                return "待接单";
            case 3:
                return "处理中";
            case 4:
                return "已完成";
            case 5:
                return "已拒绝";
            default:
                return "未知状态";
        }
    }

}
