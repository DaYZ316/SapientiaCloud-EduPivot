package com.dayz.sapientiacloud_edupivot.auth.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.auth.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.security.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String[] WHITELIST = {
            "/login",
            "/validate",
            "/register",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs",
            // 常见的通过网关转发后可能的路径形式
            "/api/auth/login",
            "/api/auth/validate",
            "/api/auth/register",
            "/api/auth/v3/api-docs",
    };
    private static final String USERID_CLAIM = "userId";
    private static final String USERNAME_CLAIM = "username";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Integer TOKEN_PREFIX_LENGTH = BEARER_PREFIX.length();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;
    private final SysUserClient sysUserClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 如果是白名单中的路径，直接放行
        if (isWhitelistPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromRequest(request);
        if (token != null) {
            try {
                // 验证令牌，此方法会检查令牌是否在黑名单中
                if (!jwtUtil.isTokenExpired(token)) {
                    DecodedJWT jwt = jwtUtil.validateToken(token);
                    String username = jwt.getSubject();
                    String userId = jwt.getClaim(USERID_CLAIM).asString();

                    if (username != null && userId != null) {
                        Result<SysUserInternalVO> userResult = sysUserClient.getUserInfoByUsername(username);
                        if (userResult == null || !userResult.isSuccess()) {
                            throw new BusinessException(SysUserEnum.USER_NOT_FOUND.getMessage());
                        }
                        SysUserInternalVO sysUserInternalVO = userResult.getData();
                        List<SysRoleVO> roles = sysUserClient.getUserRoles(sysUserInternalVO.getId()).getData();

                        // 创建用户详情对象，存储用户名和用户ID
                        Map<String, Object> userDetails = new HashMap<>();
                        userDetails.put(USERNAME_CLAIM, username);
                        userDetails.put(USERID_CLAIM, userId);

                        // 创建认证对象，使用userDetails作为principal
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                roles.stream()
                                        .filter(Objects::nonNull)
                                        .map(role -> new SimpleGrantedAuthority(role.getRoleKey()))
                                        .toList()
                        );

                        // 设置认证信息到安全上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("用户 {} (ID: {}) 已认证通过JWT令牌", username, userId);
                    }
                }
            } catch (Exception e) {
                log.error("JWT令牌验证失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            log.warn("请求未携带JWT令牌: {}", requestURI);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelistPath(String requestURI) {
        for (String pattern : WHITELIST) {
            if (pathMatcher.match(pattern, requestURI)) {
                return true;
            }
        }
        return false;
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken)) {
            // 如果已经有Bearer前缀，则去掉前缀
            if (bearerToken.startsWith(BEARER_PREFIX)) {
                return bearerToken.substring(TOKEN_PREFIX_LENGTH);
            }
            // 如果没有Bearer前缀，直接返回token
            return bearerToken;
        }
        return null;
    }
} 