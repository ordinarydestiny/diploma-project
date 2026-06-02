package com.internship.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * JWT工具类
 * 提供Token的生成、解析、验证等功能
 * Token中包含用户ID、用户名、角色编码等声明信息
 */
@Slf4j
@Component
public class JwtUtil {

    /** JWT签名密钥，从配置文件读取 */
    @Value("${jwt.secret}")
    private String secret;

    /** Token过期时间（毫秒），从配置文件读取 */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 获取签名密钥
     * 将配置的密钥字符串转换为HMAC-SHA算法所需的SecretKey对象
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     * 将用户ID、用户名、角色编码写入Token声明中
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roleCode 角色编码
     * @return 生成的JWT Token字符串
     */
    public String generateToken(Long userId, String username, String roleCode) {
        log.debug("生成Token - 用户: {}, 角色: {}", username, roleCode);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roleCode", roleCode);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析JWT Token
     *
     * @param token JWT Token字符串
     * @return Token中的声明信息
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * 从Token中获取角色编码
     */
    public String getRoleCodeFromToken(String token) {
        return parseToken(token).get("roleCode", String.class);
    }

    /**
     * 判断Token是否已过期
     *
     * @param token JWT Token字符串
     * @return true=已过期，false=未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Token过期检查异常: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 验证Token是否有效
     * 同时检查Token能否正常解析且未过期
     *
     * @param token JWT Token字符串
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从当前请求上下文中获取用户ID
     * 优先从SecurityContext获取，其次从请求头Token解析
     *
     * @return 当前登录用户的ID，未登录返回null
     */
    public Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String name = auth.getName();
                if (name != null && !name.isEmpty()) {
                    try {
                        return Long.parseLong(name);
                    } catch (NumberFormatException e) {
                        log.debug("无法将用户名转换为Long: {}", name);
                    }
                }
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    return getUserIdFromToken(token);
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID异常: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从当前请求上下文中获取当前用户角色
     *
     * @return 当前用户的角色编码，未登录返回null
     */
    public String getCurrentRole() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
                return auth.getAuthorities().iterator().next().getAuthority();
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    return getRoleCodeFromToken(token);
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户角色异常: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从当前请求上下文中获取用户名
     *
     * @return 当前用户的用户名，未登录返回null
     */
    public String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    return getUsernameFromToken(token);
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户名异常: {}", e.getMessage());
        }
        return null;
    }
}
