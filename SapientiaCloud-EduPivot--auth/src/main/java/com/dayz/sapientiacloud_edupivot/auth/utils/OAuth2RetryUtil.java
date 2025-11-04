package com.dayz.sapientiacloud_edupivot.auth.utils;

import com.dayz.sapientiacloud_edupivot.auth.constant.OAuth2Constants;
import com.dayz.sapientiacloud_edupivot.auth.enums.OAuth2Enum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.function.Supplier;


@Slf4j
public class OAuth2RetryUtil {

    private OAuth2RetryUtil() {
        // 禁止实例化
    }

    public static <T> T executeWithRetry(String operation, Supplier<T> supplier) {
        int maxRetries = OAuth2Constants.MAX_RETRIES;

        for (int i = 0; i < maxRetries; i++) {
            try {
                log.debug("执行操作: {}, 第{}次尝试", operation, i + 1);
                return supplier.get();

            } catch (ResourceAccessException e) {
                handleRetryableException(operation, e, i, maxRetries,
                        OAuth2Enum.OAUTH2_NETWORK_ERROR, "网络异常");

            } catch (HttpServerErrorException e) {
                handleRetryableException(operation, e, i, maxRetries,
                        OAuth2Enum.OAUTH2_API_ERROR, "服务器错误");

            } catch (HttpClientErrorException.Unauthorized e) {
                log.error("{} 失败: 401 未授权, 响应: {}", operation, e.getResponseBodyAsString());
                throw new BusinessException(OAuth2Enum.OAUTH2_TOKEN_INVALID);

            } catch (HttpClientErrorException.Forbidden e) {
                log.error("{} 失败: 403 权限不足, 响应: {}", operation, e.getResponseBodyAsString());
                throw new BusinessException(OAuth2Enum.OAUTH2_PERMISSION_DENIED);

            } catch (HttpClientErrorException.NotFound e) {
                log.error("{} 失败: 404 资源不存在, 响应: {}", operation, e.getResponseBodyAsString());
                throw new BusinessException(OAuth2Enum.OAUTH2_CONFIG_ERROR);

            } catch (HttpClientErrorException.BadRequest e) {
                log.error("{} 失败: 400 错误请求, 响应: {}", operation, e.getResponseBodyAsString());
                throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);

            } catch (BusinessException e) {
                throw e;

            } catch (Exception e) {
                log.warn("{} 失败，第{}次尝试: {}", operation, i + 1, e.getMessage());
                if (i == maxRetries - 1) {
                    log.error("{} 失败，已重试{}次: {}", operation, maxRetries, e.getMessage(), e);
                    throw new BusinessException(OAuth2Enum.OAUTH2_RESPONSE_INVALID);
                }
                sleepWithBackoff(i);
            }
        }

        throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
    }

    private static void handleRetryableException(
            String operation, Exception e, int currentAttempt,
            int maxRetries, OAuth2Enum errorEnum, String errorType) {
        log.warn("{} {}，第{}次尝试: {}", operation, errorType, currentAttempt + 1, e.getMessage());

        if (currentAttempt == maxRetries - 1) {
            log.error("{} {}，已重试{}次: {}", operation, errorType, maxRetries, e.getMessage(), e);
            throw new BusinessException(errorEnum);
        }

        sleepWithBackoff(currentAttempt);
    }

    private static void sleepWithBackoff(int attempt) {
        try {
            long delay = OAuth2Constants.RETRY_DELAY_BASE_MS * (attempt + 1);
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
        }
    }
}

