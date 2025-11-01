package com.dayz.sapientiacloud_edupivot.auth.security.utils;

import org.springframework.util.AntPathMatcher;

/**
 * 白名单路径判断工具类
 */
public class WhitelistUtil {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 判断请求路径是否在白名单中
     *
     * @param requestPath 请求路径
     * @param whitelist   白名单路径数组
     * @return 如果路径匹配白名单则返回true，否则返回false
     */
    public static boolean isWhitelistPath(String requestPath, String[] whitelist) {
        for (String pattern : whitelist) {
            if (PATH_MATCHER.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

    private WhitelistUtil() {
        // 禁止实例化
    }
}
