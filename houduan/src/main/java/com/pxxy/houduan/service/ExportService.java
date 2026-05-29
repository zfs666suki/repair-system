package com.pxxy.houduan.service;

import org.apache.poi.ss.usermodel.Workbook;
import java.time.LocalDate;

public interface ExportService {

    /**
     * 导出报修单为Excel
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Excel工作簿
     */
    Workbook exportRepairOrders(LocalDate startDate, LocalDate endDate);

}
