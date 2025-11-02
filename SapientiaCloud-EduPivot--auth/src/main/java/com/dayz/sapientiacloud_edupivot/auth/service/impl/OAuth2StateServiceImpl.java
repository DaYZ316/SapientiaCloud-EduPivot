package com.dayz.sapientiacloud_edupivot.auth.service.impl;

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

    /**
     * Redis key前缀
     */
    private static final String STATE_KEY_PREFIX = "oauth2:state:";

    /**
     * State过期时间（分钟）
     */
    private static final int STATE_EXPIRE_MINUTES = 10;

    @Override
    public String generateAndStoreState(String provider) {
        if (!StringUtils.hasText(provider)) {
            throw new BusinessException(OAuth2Enum.PROVIDER_NOT_SUPPORTED);
        }
        String state = UuidCreator.getTimeOrderedEpoch().toString();
        
        // 存储到Redis，key: oauth2:state:{state}, value: provider, 过期时间10分钟
        String key = STATE_KEY_PREFIX + state;
        redisTemplate.opsForValue().set(key, provider, STATE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
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

        String key = STATE_KEY_PREFIX + state;
        Object storedProvider = redisTemplate.opsForValue().get(key);
        
        // state不存在或已过期
        if (storedProvider == null) {
            log.warn("OAuth2 state验证失败: state不存在或已过期, state={}, provider={}", state, provider);
            throw new BusinessException(OAuth2Enum.STATE_NOT_FOUND);
        }
        
        // provider不匹配
        if (!provider.equals(storedProvider.toString())) {
            log.warn("OAuth2 state验证失败: provider不匹配, state={}, expected={}, actual={}", 
                    state, provider, storedProvider);
            throw new BusinessException(OAuth2Enum.STATE_INVALID);
        }
        
        // 验证成功，删除state（一次性使用，防止重放攻击）
        redisTemplate.delete(key);
        log.debug("OAuth2 state验证成功并已删除: state={}, provider={}", state, provider);
    }
}

