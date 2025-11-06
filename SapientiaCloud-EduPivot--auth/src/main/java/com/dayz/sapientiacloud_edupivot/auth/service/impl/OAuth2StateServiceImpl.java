package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.constant.OAuth2Constants;
import com.dayz.sapientiacloud_edupivot.auth.enums.OAuth2Enum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.service.OAuth2StateService;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * OAuth2 State管理服务实现类
 */
@Service
@RequiredArgsConstructor
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
            throw new BusinessException(OAuth2Enum.STATE_NOT_FOUND);
        }

        if (!provider.equals(storedProvider.toString())) {
            throw new BusinessException(OAuth2Enum.STATE_INVALID);
        }
    }
}

