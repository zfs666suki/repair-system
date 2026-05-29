package com.pxxy.houduan.common;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统一API响应结果封装类
 * 提供标准化的返回格式，包含状态码、消息和数据
 *
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> {

    private static final Logger logger = LoggerFactory.getLogger(Result.class);

    private Integer code;
    private String message;
    private T data;

    /**
     * 创建成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 状态码为200的成功响应对象
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    /**
     * 创建成功响应（携带数据）
     *
     * @param data 要返回的数据
     * @param <T>  数据类型
     * @return 状态码为200且包含数据的成功响应对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        logger.info("{}",result);//Result(code=200, message=success, data={token=Bearer eyJhb....,user=SysUser{}})
        //但在转换成JSON时，转换器会读取SysUser这个对象内部的所有 getter方法，把它的属性一个个提取出来，变成平铺的键值对（如 "id": 5, "username": "2021..."），从而隐藏了底层的类代码，只保留了纯粹的数据。
        return result;
    }

    /**
     * 创建错误响应（指定状态码和消息）
     *
     * @param code    错误状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 包含指定错误码和消息的响应对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 创建错误响应（默认500状态码）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 状态码为500的失败响应对象
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

}
