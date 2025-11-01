package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SendVerificationCodeDTO;
import com.dayz.sapientiacloud_edupivot.auth.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final RedisTemplate<String, Object> redisTemplate;

    public VerificationCodeServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String CODE_SEND_LIMIT_PREFIX = "code:send:limit:";
    
    /**
     * 验证码过期时间（分钟）
     */
    private static final int CODE_EXPIRE_MINUTES = 5;
    
    /**
     * 验证码发送间隔（秒），防止频繁发送
     */
    private static final int SEND_INTERVAL_SECONDS = 60;
    
    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 6;
    
    /**
     * 初始验证码（开发环境固定验证码）
     */
    private static final String INIT_VERIFICATION_CODE = "123456";

    @Override
    public boolean sendVerificationCode(SendVerificationCodeDTO sendVerificationCodeDTO) {
        if (sendVerificationCodeDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }

        if (!StringUtils.hasText(sendVerificationCodeDTO.getMobile())) {
            throw new BusinessException(SysUserEnum.PHONE_NUMBER_CANNOT_BE_EMPTY);
        }
        
        return sendSmsCode(sendVerificationCodeDTO.getMobile());
    }

    @Override
    public boolean verifyCode(String mobile, String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(SysUserEnum.VERIFICATION_CODE_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(mobile)) {
            throw new BusinessException(SysUserEnum.PHONE_NUMBER_CANNOT_BE_EMPTY);
        }

        String key = SMS_CODE_PREFIX + mobile;
        Object storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            throw new BusinessException(SysUserEnum.VERIFICATION_CODE_ERROR);
        }

        if (code.equals(storedCode.toString())) {
            // 验证成功后删除验证码
            redisTemplate.delete(key);
            return true;
        } else {
            throw new BusinessException(SysUserEnum.VERIFICATION_CODE_ERROR);
        }
    }

    /**
     * 发送短信验证码
     *
     * @param mobile 手机号码
     * @return 是否发送成功
     */
    private boolean sendSmsCode(String mobile) {
        // 检查发送频率限制
        String limitKey = CODE_SEND_LIMIT_PREFIX + "mobile:" + mobile;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            throw new BusinessException(SysUserEnum.VERIFICATION_CODE_SEND_TOO_FREQUENT);
        }

        // 生成验证码
        String code = generateCode();

        try {
            // TODO: 集成阿里云短信服务
            // 这里暂时只存储到Redis，实际生产环境需要调用阿里云短信API发送
            log.info("发送短信验证码到手机号: {}, 验证码: {}", mobile, code);
            
            // 存储验证码到Redis，设置过期时间
            String key = SMS_CODE_PREFIX + mobile;
            redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            // 设置发送频率限制
            redisTemplate.opsForValue().set(limitKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
            
            return true;
        } catch (Exception e) {
            log.error("发送短信验证码失败: {}", e.getMessage(), e);
            throw new BusinessException("发送短信验证码失败: " + e.getMessage());
        }
    }

    /**
     * 生成随机验证码
     * TODO: 临时使用固定验证码，后续需要改为随机生成
     *
     * @return 6位数字验证码
     */
    private String generateCode() {
        // 临时使用固定验证码（开发环境）
        return INIT_VERIFICATION_CODE;
        
        // 原有的随机生成逻辑（已暂时注释）
        // Random random = new Random();
        // StringBuilder code = new StringBuilder();
        // for (int i = 0; i < CODE_LENGTH; i++) {
        //     code.append(random.nextInt(10));
        // }
        // return code.toString();
    }
}

