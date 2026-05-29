package com.pxxy.houduan.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * JSON序列化工具类
 * 提供Java对象与JSON字符串之间的转换功能
 */
public class JsonUtils {

    // Jackson ObjectMapper实例，线程安全static可复用
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // new JavaTimeModule() 创建一个JavaTimeModule对象，用于处理Java 8的日期和时间类型
        // JavaTimeModule对象将Java 8的日期和时间类型映射为JSON格式
        // registerModule() 方法将JavaTimeModule对象注册到ObjectMapper对象中
        objectMapper.registerModule(new JavaTimeModule());

        // SerializationFeature是Jackson库中的一个枚举类，用于控制序列化JSON时的各种行为。
        // WRITE_DATES_AS_TIMESTAMPS 禁用将日期对象序列化为时间戳（Unix时间戳）
        // disable()方法禁用指定功能，这里禁用将日期对象序列化为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     *将Java对象转换为JSON字符串
     * @param obj 要转换的Java对象
     * @return String JSON格式字符串，转换失败返回null
     */
    public static String toJson(Object obj) {
        try {
            //`writeValueAsString(Object obj)`：将 Java 对象序列化为 JSON 字符串
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            //printStackTrace() 方法用于将异常的堆栈信息打印到控制台，方便调试
            e.printStackTrace();
            return null;
        }
    }

}

