package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.constants.SecurityConstants;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LogoutDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.LogoutVO;
import com.dayz.sapientiacloud_edupivot.auth.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.service.AuthService;
import com.dayz.sapientiacloud_edupivot.auth.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * 提供用户登录、Token验证和登出等功能
 * 使用JWT作为令牌生成和验证的机制，Redis进行令牌缓存
 *
 * @author DaYZ
 * @date 2025/06/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /**
     * 用户详情服务，用于加载用户信息
     * 由Spring Security提供，实现在UserDetailsServiceImpl中
     */
    private final UserDetailsService userDetailsService;

    /**
     * JWT工具类，用于生成和验证Token
     * 提供JWT令牌的创建、解析和验证功能
     */
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Redis模板，用于缓存Token
     * 存储用户Token，支持登出时令牌失效
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 认证管理器，用于处理认证请求
     * Spring Security核心组件，负责用户身份验证
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Redis中Token键前缀，用于区分不同类型的缓存
     * 格式: auth:token:{username}
     */
    private static final String TOKEN_KEY_PREFIX = "auth:token:";

    /**
     * Redis中Token黑名单键前缀，用于存储已登出的token
     * 格式: auth:blacklist:{tokenHash}
     */
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";

    /**
     * Redis中用户所有Token集合键前缀，用于管理用户的多设备登录
     * 格式: auth:user_tokens:{username}
     */
    private static final String USER_TOKENS_PREFIX = "auth:user_tokens:";

    /**
     * Redis中登出审计日志键前缀
     * 格式: auth:logout_log:{username}:{timestamp}
     */
    private static final String LOGOUT_LOG_PREFIX = "auth:logout_log:";

    @Override
    public String login(String username, String password) {
        try {
            log.debug("开始认证用户: {}", username);

            // 认证用户身份，如果认证失败会抛出AuthenticationException异常
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            
            // 加载用户详情，用于生成Token
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 生成JWT令牌
            String token = jwtTokenUtil.generateToken(userDetails);
            
            // 缓存Token到Redis（用于登出时验证和失效处理）
            String tokenKey = TOKEN_KEY_PREFIX + username;
            // 设置Token的过期时间与JWT令牌相同
            redisTemplate.opsForValue().set(tokenKey, token, SecurityConstants.TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);
            
            // 将token添加到用户token集合中（支持多设备管理）
            String userTokensKey = USER_TOKENS_PREFIX + username;
            redisTemplate.opsForSet().add(userTokensKey, token);
            // 设置集合的过期时间
            redisTemplate.expire(userTokensKey, SecurityConstants.TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);
            
            log.info("用户 {} 登录成功", username);
            
            // 返回带前缀的Token
            return SecurityConstants.TOKEN_PREFIX + token;
        } catch (AuthenticationException e) {
            // 记录认证失败日志
            log.warn("用户认证失败: {}", e.getMessage());
            // 抛出业务异常，携带401状态码
            throw new BusinessException(ResultEnum.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
    }

    /**
     * 验证Token有效性
     * 检查Token是否有效，包括JWT格式验证、是否存在于Redis中、是否在黑名单中等
     *
     * @param token JWT令牌（可能带有Bearer前缀）
     * @return 如果Token有效则返回true，否则返回false
     */
    @Override
    public boolean validateToken(String token) {
        // 去除Token前缀（如"Bearer "）
        if (token.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            token = token.substring(SecurityConstants.TOKEN_PREFIX.length());
        }

        try {
            // 首先检查token是否在黑名单中
            if (isTokenBlacklisted(token)) {
                log.debug("Token在黑名单中，验证失败");
                return false;
            }

            // 从Token中提取用户名
            String username = jwtTokenUtil.getUsernameFromToken(token);
            if (username == null) {
                return false;
            }

            // 检查Redis中是否存在该Token（说明用户未登出）
            String tokenKey = TOKEN_KEY_PREFIX + username;
            Object cachedToken = redisTemplate.opsForValue().get(tokenKey);
            if (cachedToken == null) {
                return false;
            }

            // 加载用户详情，进行完整的Token验证
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // 验证Token是否有效（包括签名、过期时间等）
            return jwtTokenUtil.validateToken(token, userDetails);
        } catch (Exception e) {
            // 记录Token验证失败日志
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查token是否在黑名单中
     * 
     * @param token JWT令牌
     * @return 如果在黑名单中返回true，否则返回false
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            String tokenHash = Integer.toHexString(token.hashCode());
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + tokenHash;
            return redisTemplate.hasKey(blacklistKey);
        } catch (Exception e) {
            log.warn("检查token黑名单状态失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 用户登出
     * 从HTTP请求中自动获取token并进行登出处理
     * 
     * @param request HTTP请求对象，用于获取token和客户端信息
     * @param logoutDTO 登出请求参数
     * @return 登出响应信息
     */
    @Override
    public LogoutVO logoutEnhanced(HttpServletRequest request, LogoutDTO logoutDTO) {
        try {
            // 从请求头中获取Token
            String token = extractTokenFromRequest(request);
            if (!StringUtils.hasText(token)) {
                throw new BusinessException(ResultEnum.UNAUTHORIZED.getCode(), "未找到有效的认证令牌");
            }

            // 获取客户端IP地址
            String clientIp = getClientIpAddress(request);

            // 调用带token的登出方法
            return logoutWithToken(token, logoutDTO, clientIp);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("增强登出失败: {}", e.getMessage(), e);
            throw new BusinessException("登出处理失败");
        }
    }

    /**
     * 用户登出（从token自动识别用户）
     * 
     * @param token JWT令牌
     * @param logoutDTO 登出请求参数
     * @param clientIp 客户端IP地址
     * @return 登出响应信息
     */
    @Override
    public LogoutVO logoutWithToken(String token, LogoutDTO logoutDTO, String clientIp) {
        // 去除Token前缀
        String cleanToken = token.startsWith(SecurityConstants.TOKEN_PREFIX) ?
            token.substring(SecurityConstants.TOKEN_PREFIX.length()) : token;

        try {
            // 验证token有效性
            if (!validateToken(token)) {
                throw new BusinessException(ResultEnum.UNAUTHORIZED.getCode(), "Token已失效或无效");
            }

            // 从Token中提取用户名
            String username = jwtTokenUtil.getUsernameFromToken(cleanToken);
            if (!StringUtils.hasText(username)) {
                throw new BusinessException(ResultEnum.UNAUTHORIZED.getCode(), "无法从Token中获取用户信息");
            }

            int invalidatedCount = 0;
            String logoutType = logoutDTO != null ? logoutDTO.getLogoutType() : "current";

            if ("all".equals(logoutType)) {
                // 登出所有设备
                invalidatedCount = invalidateAllUserTokens(username, "用户主动登出所有设备");
            } else {
                // 只登出当前设备
                invalidatedCount = invalidateCurrentToken(username, cleanToken);
            }

            // 记录登出审计日志
            logAuditEvent(username, logoutType, clientIp, logoutDTO);

            // 构建登出响应
            return LogoutVO.builder()
                    .username(username)
                    .logoutTime(LocalDateTime.now())
                    .logoutType(logoutType)
                    .invalidatedTokenCount(invalidatedCount)
                    .message("登出成功")
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("登出处理失败: {}", e.getMessage(), e);
            throw new BusinessException("登出处理失败: " + e.getMessage());
        }
    }

    /**
     * 强制用户登出所有设备
     * 
     * @param username 用户名
     * @param reason 登出原因
     * @return 失效的token数量
     */
    @Override
    public int forceLogoutAllDevices(String username, String reason) {
        try {
            int invalidatedCount = invalidateAllUserTokens(username, reason);
            
            // 记录强制登出日志
            log.warn("管理员强制用户 {} 登出所有设备，原因: {}, 失效Token数量: {}", 
                    username, reason, invalidatedCount);
            
            return invalidatedCount;
        } catch (Exception e) {
            log.error("强制登出失败: {}", e.getMessage(), e);
            throw new BusinessException("强制登出失败: " + e.getMessage());
        }
    }

    /**
     * 从HTTP请求中提取Token
     * 
     * @param request HTTP请求对象
     * @return Token字符串，如果未找到则返回null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // 首先尝试从Authorization头获取
        String bearerToken = request.getHeader(SecurityConstants.AUTH_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(SecurityConstants.TOKEN_PREFIX.length());
        }

        // 如果Authorization头中没有，尝试从请求参数中获取
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam.startsWith(SecurityConstants.TOKEN_PREFIX) ?
                tokenParam.substring(SecurityConstants.TOKEN_PREFIX.length()) : tokenParam;
        }

        return null;
    }

    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 失效当前Token
     * 
     * @param username 用户名
     * @param token 当前token
     * @return 失效的token数量
     */
    private int invalidateCurrentToken(String username, String token) {
        // 删除Redis中的Token
        String tokenKey = TOKEN_KEY_PREFIX + username;
        redisTemplate.delete(tokenKey);

        // 将token加入黑名单
        addTokenToBlacklist(token);

        // 从用户token集合中移除
        String userTokensKey = USER_TOKENS_PREFIX + username;
        redisTemplate.opsForSet().remove(userTokensKey, token);

        log.info("用户 {} 当前设备已登出", username);
        return 1;
    }

    /**
     * 失效用户所有Token
     * 
     * @param username 用户名
     * @param reason 登出原因
     * @return 失效的token数量
     */
    private int invalidateAllUserTokens(String username, String reason) {
        int count = 0;

        // 获取用户所有token
        String userTokensKey = USER_TOKENS_PREFIX + username;
        Set<Object> userTokens = redisTemplate.opsForSet().members(userTokensKey);

        if (userTokens != null && !userTokens.isEmpty()) {
            // 将所有token加入黑名单
            for (Object tokenObj : userTokens) {
                String token = tokenObj.toString();
                addTokenToBlacklist(token);
                count++;
            }

            // 清空用户token集合
            redisTemplate.delete(userTokensKey);
        }

        // 删除主要的token缓存
        String tokenKey = TOKEN_KEY_PREFIX + username;
        if (redisTemplate.hasKey(tokenKey)) {
            redisTemplate.delete(tokenKey);
            if (count == 0) {
                count = 1;
            }
        }

        log.info("用户 {} 所有设备已登出，原因: {}, 失效Token数量: {}", username, reason, count);
        return count;
    }

    /**
     * 将token加入黑名单
     * 
     * @param token token
     */
    private void addTokenToBlacklist(String token) {
        try {
            // 生成token的hash作为键名
            String tokenHash = Integer.toHexString(token.hashCode());
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + tokenHash;
            
            // 在黑名单中设置过期时间，与token原本的过期时间一致
            redisTemplate.opsForValue().set(blacklistKey, "blacklisted", SecurityConstants.TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("添加token到黑名单失败: {}", e.getMessage());
        }
    }

    /**
     * 记录登出审计日志
     * 
     * @param username 用户名
     * @param logoutType 登出类型
     * @param clientIp 客户端IP
     * @param logoutDTO 登出请求
     */
    private void logAuditEvent(String username, String logoutType, String clientIp, LogoutDTO logoutDTO) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String logKey = LOGOUT_LOG_PREFIX + username + ":" + timestamp;

            // 构建审计日志数据
            String logData = String.format(
                    "用户: %s, 登出类型: %s, IP: %s, 时间: %s, 客户端: %s, 设备: %s",
                    username,
                    logoutType,
                    clientIp,
                    LocalDateTime.now(),
                    logoutDTO != null ? logoutDTO.getClientInfo() : "未知",
                    logoutDTO != null ? logoutDTO.getDeviceId() : "未知"
            );

            // 存储审计日志，保留7天
            redisTemplate.opsForValue().set(logKey, logData, 7, TimeUnit.DAYS);
            
            log.info("用户登出审计: {}", logData);
        } catch (Exception e) {
            log.warn("记录登出审计日志失败: {}", e.getMessage());
        }
    }
} 