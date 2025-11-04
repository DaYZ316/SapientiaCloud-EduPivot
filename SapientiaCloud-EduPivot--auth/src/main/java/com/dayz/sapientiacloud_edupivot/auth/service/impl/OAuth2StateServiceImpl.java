package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.constant.OAuth2Constants;
import com.dayz.sapientiacloud_edupivot.auth.enums.OAuth2Enum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.service.OAuth2StateService;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * OAuth2 State管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2StateServiceImpl implements OAuth2StateService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public String generateAndStoreState(String provider) {
        if (!StringUtils.hasText(provider)) {
            throw new BusinessException(OAuth2Enum.PROVIDER_NOT_SUPPORTED);
        }
        String state = UuidCreator.getTimeOrderedEpoch().toString();

        String key = OAuth2Constants.REDIS_STATE_KEY_PREFIX + state;
        redisTemplate.opsForValue().set(key, provider, OAuth2Constants.STATE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.debug("生成并存储OAuth2 state: provider={}, state={}", provider, state);

        return state;
    }

    @Override
    public void validateState(String state, String provider) {
        if (!StringUtils.hasText(state)) {
            throw new BusinessException(OAuth2Enum.STATE_INVALID);
        }

        if (!StringUtils.hasText(provider)) {
            throw new BusinessException(OAuth2Enum.PROVIDER_NOT_SUPPORTED);
        }

        String key = OAuth2Constants.REDIS_STATE_KEY_PREFIX + state;

        Object storedProvider = redisTemplate.opsForValue().getAndDelete(key);

        if (storedProvider == null) {
            log.warn("OAuth2 state验证失败: state不存在、已过期或已被使用, state={}, provider={}", state, provider);
            throw new BusinessException(OAuth2Enum.STATE_NOT_FOUND);
        }

        if (!provider.equals(storedProvider.toString())) {
            log.warn("OAuth2 state验证失败: provider不匹配, state={}, expected={}, actual={}",
                    state, provider, storedProvider);
            throw new BusinessException(OAuth2Enum.STATE_INVALID);
        }

        // 验证成功，state已通过原子操作删除，防止重放攻击
        log.debug("OAuth2 state验证成功并已删除: state={}, provider={}", state, provider);
    }
}

