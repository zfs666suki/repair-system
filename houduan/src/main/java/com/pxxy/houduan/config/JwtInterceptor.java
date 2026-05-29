package com.pxxy.houduan.config;

import com.pxxy.houduan.common.JwtUtils;
import com.pxxy.houduan.common.JsonUtils;
import com.pxxy.houduan.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT认证拦截器
 * 拦截请求并验证JWT令牌的有效性，实现统一的身份认证
 */
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);

    private final JwtUtils jwtUtils;

    public JwtInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 预处理请求，验证JWT令牌
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  被调用的处理器对象
     * @return true表示验证通过，允许请求继续；false表示验证失败，请求被拦截
     * @throws Exception 处理过程中的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取Authorization令牌
        String token = request.getHeader("Authorization");
        //token == null：防止请求头中不存在 Authorization 字段时出现空指针异常
        //token.isEmpty()：防止字段存在但值为空字符串的情况
        if (token == null || token.isEmpty()) {
            logger.warn("未授权访问: 缺少token, path={}", request.getRequestURI());
            returnJson(response, Result.error(401, "未授权"));
            return false;
        }

        // 去除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证令牌有效性
        if (!jwtUtils.validateToken(token)) {
            logger.warn("未授权访问: token无效或过期, path={}", request.getRequestURI());
            returnJson(response, Result.error(401, "token已过期或无效"));
            return false;
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        Integer role = jwtUtils.getRoleFromToken(token);
        logger.info("授权访问: userId={}, role={}, path={}", userId, role, request.getRequestURI());

        return true;
    }

    /**
     * 向客户端返回JSON格式的响应
     *
     * @param response HTTP响应对象
     * @param result   要返回的结果对象
     * @throws IOException IO异常
     */
    private void returnJson(HttpServletResponse response, Result<?> result) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.print(JsonUtils.toJson(result));
        writer.close();
    }

}
