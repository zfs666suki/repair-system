package com.pxxy.houduan.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * 全局异常处理器
 * 实现HandlerExceptionResolver接口，统一捕获和处理系统中未被捕获的异常
 * 将异常信息转换为统一的JSON格式返回给前端
 */
@Component
public class GlobalExceptionHandler implements HandlerExceptionResolver {

    /**
     * LoggerFactory 来自 SLF4J（Simple Logging Facade for Java）依赖。
     * 作用：SLF4J 提供的工厂类，用于创建 Logger 实例；
     * 实际实现：Spring Boot 默认使用 Logback 作为 SLF4J 的实现
      */
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    /**
     * ObjectMapper 来自 Jackson 库。
     * 作用：Jackson 的核心类，用于 Java 对象与 JSON 之间的序列化和反序列化转换；
     * 实际实现：在此代码中用于将 Result 对象转换为 JSON 格式并写入响应流
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理系统中未捕获的异常
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象，用于设置响应状态码和内容类型
     * @param handler  被调用的处理器对象
     * @param ex       发生的异常对象
     * @return 空的ModelAndView对象，表示已自行处理响应
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        response.setContentType("application/json; charset=UTF-8");//设置响应内容类型为JSON 字符编码为UTF-8

        Result<?> result;

        if (ex instanceof IllegalArgumentException) {     //IllegalArgumentException 来自 Java 语言，用于处理参数错误
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            //SC_BAD_REQUEST 状态码表示请求参数错误 400
            logger.warn("参数异常: {}", ex.getMessage());
            result = Result.error(ex.getMessage());

        } else if (ex instanceof NullPointerException) {  //NullPointerException 来自 Java 语言，用于处理空指针错误
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            //SC_INTERNAL_SERVER_ERROR 状态码表示服务器内部错误 500
            logger.error("空指针异常: {}", ex.getMessage(), ex);
            result = Result.error("系统异常，请稍后重试");
        } else if (ex instanceof RuntimeException) {  //RuntimeException 来自 Java 语言，用于处理运行时错误
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.error("运行时异常: {}", ex.getMessage(), ex);
            result = Result.error(ex.getMessage());
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.error("全局异常处理: {}", ex.getMessage(), ex);
            result = Result.error("系统异常，请稍后重试");
        }


        try {
            //response.getWriter()获取获取的是用于向HTTP响应体（Response Body）写入字符数据的的输出流PrintWriter对象
            //writeValue()方法将 result 对象序列化为JSON并直接写入输出流（这个 Writer）
            objectMapper.writeValue(response.getWriter(), result);
        } catch (IOException e) {
            logger.error("异常处理失败: {}", e.getMessage(), e);
        }
        /*
        返回空的 ModelAndView 对象，告知 Spring MVC 框架：
            异常已处理：响应已通过 response.getWriter() 直接写入
            无需视图渲染：框架不需要再进行后续处理
            结束请求流程：防止框架继续尝试其他异常处理器
        */
        return new ModelAndView();
    }
/*`logger` 对象来自 SLF4J 的 `Logger` 接口。
- **常用方法**：
  - `debug(String format, Object... arguments)`：调试级别日志
  - `info(String format, Object... arguments)`：信息级别日志
  - `warn(String format, Object... arguments)`：警告级别日志，用于潜在问题
  - `error(String format, Object... arguments)`：错误级别日志，用于严重问题
  - `trace(String format, Object... arguments)`：追踪级别日志，最详细的日志
- **参数说明**：
  - `format`：使用 `{}` 作为占位符的日志模板
  - `arguments`：替换占位符的实际值，可以是多个参数或异常对象
- **实际使用**：在此代码中，`warn` 记录参数异常，`error` 记录系统级异常并附带完整堆栈信息*/

/*`ObjectMapper` 来自 Jackson 库，是 JSON 处理的核心类。
- **常用方法**：
  - `writeValueAsString(Object obj)`：将 Java 对象序列化为 JSON 字符串
  - `writeValue(Writer writer, Object value)`：将 Java 对象序列化并写入输出流（如响应流）
  - `readValue(String content, Class<T> valueType)`：将 JSON 字符串反序列化为 Java 对象
  - `convertValue(Object fromValue, Class<T> toValueType)`：在不同类型的 Java 对象之间转换
  - `createObjectNode()` / `createArrayNode()`：创建空的 JSON 对象或数组节点
- **参数说明**（以 `writeValue` 为例）：
  - `writer`：目标输出流，如 `response.getWriter()`
  - `value`：要序列化的 Java 对象
- **实际使用**：在此代码中，`writeValue` 直接将 `Result` 对象转换为 JSON 并写入 HTTP 响应流，避免中间字符串的内存开销*/
}

