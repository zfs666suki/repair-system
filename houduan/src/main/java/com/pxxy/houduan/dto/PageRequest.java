package com.pxxy.houduan.dto;

/**
 * 分页请求DTO
 * 用于统一分页查询参数
 */
public class PageRequest {

    /**
     * 当前页码，默认第1页
     */
    private long pageNum = 1;

    /**
     * 每页大小，默认10条
     */
    private long pageSize = 10;

    public PageRequest() {
    }

    public PageRequest(long pageNum, long pageSize) {
        this.pageNum = pageNum > 0 ? pageNum : 1;
        this.pageSize = pageSize > 0 ? pageSize : 10;
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum > 0 ? pageNum : 1;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize > 0 ? pageSize : 10;
    }
}