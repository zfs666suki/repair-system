package com.pxxy.houduan.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装类
 * 用于统一返回分页查询的结果数据，包含状态码、消息和分页数据
 * 支持泛型，可适配不同类型的记录列表
 * @param <T> 记录数据类型
 */
@Data
public class PageResult<T> {

    private Integer code;
    private String message;
    private PageData<T> data;

    /**
     * 创建成功的分页结果
     *
     * @param records 当前页的记录列表
     * @param total   总记录数
     * @param current 当前页码
     * @param size    每页显示条数
     * @param <T>     记录数据类型
     * @return 包含分页数据的成功结果对象，状态码为200
     */
    public static <T> PageResult<T> success(List<T> records, long total, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(new PageData<>(records, total, current, size));
        return result;
    }

    /**
     * 创建失败的分页结果
     *
     * @param message 错误消息
     * @param <T>     记录数据类型
     * @return 包含错误信息的失败结果对象，状态码为500
     */
    public static <T> PageResult<T> error(String message) {
        PageResult<T> result = new PageResult<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 分页数据内部类
     * 封装分页查询的具体数据，包括记录列表和分页信息
     * @param <T> 记录数据类型
     */
    @Data
    public static class PageData<T> {
        private List<T> records;
        private long total;
        private long current;
        private long size;
        private long pages;

        /**
         * 构造分页数据对象
         * 自动计算总页数
         *
         * @param records 当前页的记录列表
         * @param total   总记录数
         * @param current 当前页码
         * @param size    每页显示条数
         */
        public PageData(List<T> records, long total, long current, long size) {
            this.records = records;
            this.total = total;
            this.current = current;
            this.size = size;
            this.pages = size > 0 ? (total + size - 1) / size : 0;
        }
    }
}
