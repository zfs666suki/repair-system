package com.pxxy.houduan.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * JWT工具类，用于生成、解析和验证JWT令牌
 * 从配置文件中读取jwt.secret（密钥）和jwt.expiration（过期时间）
 */
@Data
@Component
//从配置文件中读取jwt.secret（密钥）和jwt.expiration（过期时间）
@ConfigurationProperties(prefix = "jwt")
public class JwtUtils {

    private String secret;
    private long expiration;

    /**
     * 生成JWT令牌
     *
     * @param userId 用户ID
     * @param role 用户角色
     * @return 生成的JWT令牌字符串
     */
    public String generateToken(Long userId, Integer role) {
        // 计算令牌过期时间
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        // 构建载荷数据，包含用户ID和角色
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        // 使用HS256算法签名并生成JWT令牌
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
        //eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjozLCJleHAiOjE3ODY0NDkxMjcsInVzZXJJZCI6MSwiaWF0IjoxNzc3ODA5MTI3fQ.
        //scUd1vX8tW_ONu4QzHIFPhr1vg4Dea2AN3T9yG-_fHs
    }

    /**
     * 从JWT令牌中解析声明信息
     *
     * @param token JWT令牌字符串
     * @return 声明对象，解析失败返回null
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            //{role=3, exp=1785827035, userId=1, iat=1777187035}
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证JWT令牌是否有效
     *
     * @param token JWT令牌字符串
     * @return true表示令牌有效，false表示令牌无效或已过期
     */
    public boolean validateToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return false;
        }
        //claims.getExpiration()：获取令牌的过期时间
        //.after(new Date())：判断过期时间是否晚于当前时间
        return claims.getExpiration().after(new Date());
    }

    /**
     * 从JWT令牌中获取用户ID
     *
     * @param token JWT令牌字符串
     * @return 用户ID，解析失败返回null
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从JWT令牌中获取用户角色
     *
     * @param token JWT令牌字符串
     * @return 用户角色，解析失败返回null
     */
    public Integer getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return (Integer) claims.get("role");
    }

    /**
     * 从JWT令牌中获取用户ID（自动处理Bearer前缀）
     *
     * @param token JWT令牌字符串（可包含Bearer前缀）
     * @return 用户ID，解析失败返回null
     */
    public Long getUserIdFromTokenWithBearer(String token) {
        String cleanToken = extractToken(token);
        return getUserIdFromToken(cleanToken);
    }

    /**
     * 从JWT令牌中获取用户角色（自动处理Bearer前缀）
     *
     * @param token JWT令牌字符串（可包含Bearer前缀）
     * @return 用户角色，解析失败返回null
     */
    public Integer getRoleFromTokenWithBearer(String token) {
        String cleanToken = extractToken(token);
        return getRoleFromToken(cleanToken);
    }

    /**
     * 验证JWT令牌是否有效（自动处理Bearer前缀）
     *
     * @param token JWT令牌字符串（可包含Bearer前缀）
     * @return true表示令牌有效，false表示令牌无效或已过期
     */
    public boolean validateTokenWithBearer(String token) {
        String cleanToken = extractToken(token);
        return validateToken(cleanToken);
    }

    /**
     * 提取纯净的token（移除Bearer前缀）
     *
     * @param token 原始token字符串
     * @return 纯净的token字符串
     */
    private String extractToken(String token) {
        if (token == null) {
            return null;
        }
        token = token.trim();
        if (token.startsWith("Bearer ") || token.startsWith("bearer ")) {
            return token.substring(7);
        }
        return token;
    }

/*
JwtUtils 类中使用了 JJWT 库的两个核心对象方法：
1. Jwts.builder() - 构建 JWT 令牌
    包名：io.jsonwebtoken.Jwts
    作用：创建 JwtBuilder 实例，用于生成 JWT
    常用方法：
    setClaims(Map<String, Object> claims)：设置载荷（声明信息）
    setIssuedAt(Date issuedAt)：设置签发时间
    setExpiration(Date expiration)：设置过期时间
    signWith(SignatureAlgorithm algorithm, String secret)：使用指定算法和密钥签名
    compact()：生成最终的 JWT 字符串
2. Jwts.parser() - 解析 JWT 令牌
    作用：创建 JwtParser 实例，用于验证和解析 JWT
    常用方法：
    setSigningKey(String secret)：设置验签密钥
    parseClaimsJws(String token)：解析并验证 JWT，返回 Jws<Claims> 对象
    getBody()：获取载荷部分（即声明信息）
    这两个方法分别对应 JWT 的生成和解析两个核心操作。
*/
}

