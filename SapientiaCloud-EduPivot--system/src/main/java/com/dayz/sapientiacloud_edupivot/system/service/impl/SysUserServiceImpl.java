package com.dayz.sapientiacloud_edupivot.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserAdminDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserRegisterDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysUser;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserVO;
import com.dayz.sapientiacloud_edupivot.system.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.GenderEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.SysUserExceptionEnum;
import com.dayz.sapientiacloud_edupivot.system.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysUserMapper;
import com.dayz.sapientiacloud_edupivot.system.service.ISysUserService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.javafaker.Faker;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final static int DEFAULT_USERNAME_LENGTH = 8;
    private final static String INIT_PASSWORD = "123456";

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageInfo<SysUserVO> listSysUser(SysUserQueryDTO sysUserQueryDTO) {
        return PageHelper.startPage(sysUserQueryDTO.getPageNum(), sysUserQueryDTO.getPageSize())
                .doSelectPageInfo(() -> sysUserMapper.listSysUser(sysUserQueryDTO));
    }

    @Override
    @Cacheable(value = "sysUser", key = "#p0", condition = "#p0 != null")
    public SysUserVO getUserById(UUID id) {
        if (id == null) {
            throw new BusinessException(SysUserExceptionEnum.USER_NOT_FOUND.getMessage());
        }
        SysUser sysUser = this.getById(id);
        if (sysUser == null) {
            throw new BusinessException(SysUserExceptionEnum.USER_NOT_FOUND.getMessage());
        }
        SysUserVO sysUserVO = new SysUserVO();
        BeanUtils.copyProperties(sysUser, sysUserVO);
        return sysUserVO;
    }

    @Override
    @CacheEvict(value = "sysUser", key = "#result.id", condition = "#result != null")
    public SysUserVO addUser(SysUserAdminDTO sysUserAdminDTO) {
        if (sysUserAdminDTO == null) {
        throw new BusinessException(SysUserExceptionEnum.USERNAME_CANNOT_BE_EMPTY.getMessage());
        }
        if (sysUserAdminDTO.getId() != null && this.getById(sysUserAdminDTO.getId()) != null) {
            throw new BusinessException(SysUserExceptionEnum.USERNAME_ALREADY_EXISTS.getMessage());
        }
        if (sysUserMapper.selectByUsername(sysUserAdminDTO.getUsername()) != null) {
            throw new BusinessException(SysUserExceptionEnum.USERNAME_ALREADY_EXISTS.getMessage());
        }

        SysUser sysUser = checkSysUserInfo(sysUserAdminDTO);
        if (sysUser.getId() == null) {
            sysUser.setId(UuidCreator.getTimeOrderedEpoch());
        } else {
            sysUserMapper.deleteUserById(sysUser.getId());
        }
        this.save(sysUser);

        SysUserVO sysUserVO = new SysUserVO();
        BeanUtils.copyProperties(sysUser, sysUserVO);

        return sysUserVO;
    }

    @Override
    @CacheEvict(value = "sysUser", key = "#result.id", condition = "#result != null")
    public SysUserVO updateUser(SysUserDTO sysUserDTO) {
        if (sysUserDTO.getId() == null) {
            throw new BusinessException(SysUserExceptionEnum.USER_NOT_FOUND.getMessage());
        }

        SysUser sysUser = this.getById(sysUserDTO.getId());
        if (sysUser == null) {
            throw new BusinessException(SysUserExceptionEnum.USER_NOT_FOUND.getMessage());
        }

        BeanUtils.copyProperties(sysUserDTO, sysUser);

        if (sysUser.getGender() == null || GenderEnum.isCodeBetween(sysUser.getGender())) {
            sysUser.setGender(null);
        }

        sysUser.setUpdateTime(sysUserDTO.getUpdateTime());

        this.updateById(sysUser);

        SysUserVO sysUserVO = new SysUserVO();
        BeanUtils.copyProperties(sysUser, sysUserVO);

        return sysUserVO;
    }

    @Override
    @CacheEvict(value = "sysUser", key = "#p0",  condition = "#p0 != null")
    public Boolean deleteUser(UUID id) {
        if (id != null) {
            SysUser sysUser = this.getById(id);

            if (sysUser == null || sysUser.getStatus().equals(StatusEnum.DISABLED.getCode())) {
                throw new BusinessException(SysUserExceptionEnum.USER_NOT_FOUND.getMessage());
            }

            return this.removeById(id);
        } else {
            throw new BusinessException(SysUserExceptionEnum.USER_NOT_FOUND.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "sysUser", key = "#result.id", condition = "#result != null")
    public SysUserVO registerUser(SysUserRegisterDTO sysUserRegisterDTO) {
        if (sysUserRegisterDTO == null) {
            throw new BusinessException(SysUserExceptionEnum.USERNAME_CANNOT_BE_EMPTY.getMessage());
        }
        if (sysUserMapper.selectByUsername(sysUserRegisterDTO.getUsername()) != null) {
            throw new BusinessException(SysUserExceptionEnum.USERNAME_ALREADY_EXISTS.getMessage());
        }

        SysUser sysUser = checkSysUserInfo(sysUserRegisterDTO);
        sysUser.setId(UuidCreator.getTimeOrderedEpoch());
        this.save(sysUser);

        SysUserVO sysUserVO = new SysUserVO();
        BeanUtils.copyProperties(sysUser, sysUserVO);

        return sysUserVO;
    }

    public SysUser checkSysUserInfo (Object sysUserInfo) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserInfo, sysUser);

        if (!StringUtils.hasText(sysUser.getUsername())) {
            sysUser.setUsername(RandomStringUtils.randomAlphanumeric(DEFAULT_USERNAME_LENGTH));
        }
        if (!StringUtils.hasText(sysUser.getNickName())) {
            sysUser.setNickName(new Faker().name().fullName());
        }
        if (sysUser.getGender() == null || !GenderEnum.isCodeBetween(sysUser.getGender())) {
            sysUser.setGender(GenderEnum.UNKNOWN.getCode());
        }
        if (!StringUtils.hasText(sysUser.getPassword())) {
            String encodePassword = passwordEncoder.encode(INIT_PASSWORD);
            sysUser.setPassword(encodePassword);
        } else {
            sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        }
        sysUser.setStatus(StatusEnum.NORMAL.getCode());
        LocalDateTime now = LocalDateTime.now();
        sysUser.setCreateTime(now);
        sysUser.setUpdateTime(now);
        sysUser.setStatus(DeletedEnum.NOT_DELETED.getCode());
        return sysUser;
    }

    @Override
    @CachePut(value = "sysUser", key = "#result.id", condition = "#result != null")
    public SysUser selectUserByUsername(String username) {
        SysUser sysUser = sysUserMapper.selectByUsername(username);

        if (sysUser == null) {
            throw new BusinessException(SysUserExceptionEnum.USER_NOT_FOUND.getMessage());
        }

        return sysUser;
    }
}