package com.dayz.sapientiacloud_edupivot.auth.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.common.security.constants.JwtConstants;
import com.dayz.sapientiacloud_edupivot.auth.common.security.utils.TokenExtractionUtil;
import com.dayz.sapientiacloud_edupivot.auth.common.security.utils.WhitelistUtil;
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
            "/send-code",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/api/auth/**",
            "/mobile-login"
    };
    private final JwtUtil jwtUtil;
    private final SysUserClient sysUserClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 如果是白名单中的路径，直接放行
        if (WhitelistUtil.isWhitelistPath(requestURI, WHITELIST)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = TokenExtractionUtil.extractTokenFromRequest(request);
        if (token != null) {
            try {
                // 验证令牌，此方法会检查令牌是否在黑名单中
                if (!jwtUtil.isTokenExpired(token)) {
                    DecodedJWT jwt = jwtUtil.validateToken(token);
                    String username = jwt.getSubject();
                    String userId = jwt.getClaim(JwtConstants.USERID_CLAIM).asString();

                    if (username != null && userId != null) {
                        Result<SysUserInternalVO> userResult = sysUserClient.getUserInfoByUsername(username);
                        if (userResult == null || !userResult.isSuccess()) {
                            throw new BusinessException(SysUserEnum.USER_NOT_FOUND.getMessage());
                        }
                        SysUserInternalVO sysUserInternalVO = userResult.getData();
                        List<SysRoleVO> roles = sysUserClient.getUserRoles(sysUserInternalVO.getId()).getData();

                        // 创建用户详情对象，存储用户名和用户ID
                        Map<String, Object> userDetails = new HashMap<>();
                        userDetails.put(JwtConstants.USERNAME_CLAIM, username);
                        userDetails.put(JwtConstants.USERID_CLAIM, userId);

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

} 