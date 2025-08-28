package com.dayz.sapientiacloud_edupivot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.system.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.system.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.system.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.system.common.security.service.PermissionService;
import com.dayz.sapientiacloud_edupivot.system.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.*;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysRole;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysUser;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserVO;
import com.dayz.sapientiacloud_edupivot.system.enums.GenderEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.SysRoleEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysRoleMapper;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysUserMapper;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysUserPermissionMapper;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysUserRoleMapper;
import com.dayz.sapientiacloud_edupivot.system.service.ISysUserService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.javafaker.Faker;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService, UserDetailsService {

    private final static int DEFAULT_USERNAME_LENGTH = 8;
    private final static String INIT_PASSWORD = "123456";

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final SysUserPermissionMapper sysUserPermissionMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PermissionService permissionService;

    @Override
    public PageInfo<SysUserVO> listSysUserPage(SysUserQueryDTO sysUserQueryDTO) {
        if (sysUserQueryDTO == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        return PageHelper.startPage(sysUserQueryDTO.getPageNum(), sysUserQueryDTO.getPageSize())
                .doSelectPageInfo(() -> sysUserMapper.listSysUser(sysUserQueryDTO));
    }

    @Override
    public List<SysUserVO> listAllSysUser() {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SysUser::getCreateTime);

        List<SysUser> sysUserList = this.list(queryWrapper);

        return sysUserList.stream().map(sysUser -> {
            SysUserVO sysUserVO = new SysUserVO();
            BeanUtils.copyProperties(sysUser, sysUserVO);

            return sysUserVO;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "SysUser", key = "#p0", condition = "#p0 != null")
    public SysUserVO getUserById(UUID id) {
        if (id == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUser sysUser = this.getById(id);
        if (sysUser == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUserVO sysUserVO = new SysUserVO();
        BeanUtils.copyProperties(sysUser, sysUserVO);

        List<SysRoleVO> roles = sysUserRoleMapper.getUserRoles(id);
        sysUserVO.setRoles(roles);

        return sysUserVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean registerUser(SysUserRegisterDTO sysUserRegisterDTO) {
        if (sysUserRegisterDTO == null || !StringUtils.hasText(sysUserRegisterDTO.getUsername())) {
            throw new BusinessException(SysUserEnum.USERNAME_CANNOT_BE_EMPTY);
        }
        if (sysUserMapper.selectByUsername(sysUserRegisterDTO.getUsername()) != null) {
            throw new BusinessException(SysUserEnum.USERNAME_ALREADY_EXISTS);
        }

        SysUser sysUser = checkSysUserInfo(sysUserRegisterDTO);
        sysUser.setId(UuidCreator.getTimeOrderedEpoch());

        if (!StringUtils.hasText(sysUserRegisterDTO.getNickName())) {
            sysUser.setNickName(sysUser.getId().toString());
        }

        return this.save(sysUser);
    }

    @Override
    public Boolean updatePassword(SysUserPasswordDTO sysUserPasswordDTO) {
        if (sysUserPasswordDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }

        SysUserInternalVO currentUser = UserContextUtil.getCurrentUser();

        currentUser.setPassword(passwordEncoder.encode(sysUserPasswordDTO.getNewPassword()));
        currentUser.setUpdateTime(LocalDateTime.now());

        return this.updateById(currentUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "SysUser", key = "#p0.id", condition = "#p0.id != null")
    public Boolean updateUser(SysUserDTO sysUserDTO) {
        if (sysUserDTO == null || sysUserDTO.getId() == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUser sysUser = this.getById(sysUserDTO.getId());
        if (sysUser == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        BeanUtils.copyProperties(sysUserDTO, sysUser);

        if (sysUser.getGender() == null || GenderEnum.isCodeBetween(sysUser.getGender())) {
            sysUser.setGender(null);
        }

        sysUser.setUpdateTime(LocalDateTime.now());

        return this.updateById(sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "SysUser", key = "#p0", condition = "#p0 != null")
    public Boolean removeUserById(UUID id) {
        if (id == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUser sysUser = this.getById(id);
        if (sysUser == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        List<SysRoleVO> roles = sysUserRoleMapper.getUserRoles(id);
        roles.forEach(role -> {
            if (role.isAdmin()) {
                throw new BusinessException(SysRoleEnum.ADMIN_OPERATION_FORBIDDEN);
            }
        });

        boolean removed = this.removeById(id);
        if (Boolean.TRUE.equals(removed)) {
            sysUserRoleMapper.removeRolesByUserId(id);
        }

        return removed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "SysUser", allEntries = true)
    public Integer removeUserByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        List<SysUser> sysUsers = this.listByIds(ids);
        ids.forEach(id -> {
            if (sysUsers.stream().noneMatch(user -> user.getId().equals(id))) {
                throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
            }
        });

        List<SysRole> roles = sysUserRoleMapper.getRolesByUserIds(ids);
        roles.forEach(role -> {
            if (role.isAdmin()) {
                throw new BusinessException(SysRoleEnum.ADMIN_OPERATION_FORBIDDEN);
            }
        });

        int count = this.removeByIds(ids) ? ids.size() : 0;
        if (count > 0) {
            sysUserRoleMapper.removeRolesByUserIds(ids);
        }

        return Math.toIntExact(count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "SysUser", key = "#result.id", condition = "#result != null")
    public SysUserVO addUser(SysUserAdminDTO sysUserAdminDTO) {
        if (sysUserAdminDTO == null) {
            throw new BusinessException(SysUserEnum.USERNAME_CANNOT_BE_EMPTY);
        }
        if (sysUserMapper.selectByUsername(sysUserAdminDTO.getUsername()) != null) {
            throw new BusinessException(SysUserEnum.USERNAME_ALREADY_EXISTS);
        }

        SysUser sysUser = checkSysUserInfo(sysUserAdminDTO);

        if (sysUser.getId() == null) {
            sysUser.setId(UuidCreator.getTimeOrderedEpoch());
        } else {
            int result = sysUserMapper.deleteById(sysUser.getId());
            if (result <= 0) {
                throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
            }
        }
        this.save(sysUser);

        SysUserVO sysUserVO = new SysUserVO();
        BeanUtils.copyProperties(sysUser, sysUserVO);

        return sysUserVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "SysUser", key = "#result.id", condition = "#result != null")
    public SysUserVO updateProfile(SysUserProfileDTO sysUserProfileDTO) {
        if (sysUserProfileDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }

        SysUser currentUser = this.getById(UserContextUtil.getCurrentUserId());

        BeanUtils.copyProperties(sysUserProfileDTO, currentUser);
        currentUser.setUpdateTime(LocalDateTime.now());

        this.updateById(currentUser);

        SysUserVO sysUserVO = new SysUserVO();
        BeanUtils.copyProperties(currentUser, sysUserVO);

        return sysUserVO;
    }

    @Override
    @Transactional(readOnly = true)
    public SysUserInternalVO selectUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(SysUserEnum.USERNAME_CANNOT_BE_EMPTY);
        }

        SysUser sysUser = sysUserMapper.selectByUsername(username);

        if (sysUser == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUserInternalVO sysUserInternalVO = new SysUserInternalVO();
        BeanUtils.copyProperties(sysUser, sysUserInternalVO);

        sysUserInternalVO.setRoles(sysUserRoleMapper.getUserRoles(sysUser.getId()));
        sysUserInternalVO.setPermissions(sysUserPermissionMapper.getUserPermissions(sysUser.getId()));

        return sysUserInternalVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "SysUser", key = "#p0", condition = "#p0 != null")
    public Boolean assignRoles(UUID userId, List<UUID> newRoleIds) {
        if (userId == null || this.getById(userId) == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        List<UUID> existingRoleIds = sysUserRoleMapper.getUserRoleIds(userId);

        if (!existingRoleIds.isEmpty()) {
            List<SysRoleVO> exitingRoles = sysRoleMapper.getRolesByIds(existingRoleIds);
            if (exitingRoles.stream().anyMatch(SysRoleVO::isAdmin)) {
                throw new BusinessException(SysUserEnum.ADMIN_OPERATION_FORBIDDEN);
            }
        }

        Set<UUID> newRoleSet = new HashSet<>(newRoleIds);
        Set<UUID> existingRoleSet = new HashSet<>(existingRoleIds);

        List<UUID> rolesToAdd = newRoleIds.stream()
                .filter(newRoleId -> !existingRoleSet.contains(newRoleId))
                .toList();

        List<UUID> rolesToRemove = existingRoleIds.stream()
                .filter(existingRoleId -> !newRoleSet.contains(existingRoleId))
                .toList();

        if (!rolesToAdd.isEmpty()) {
            int result = sysUserRoleMapper.addUserRoles(userId, rolesToAdd);
            if (result <= 0) {
                throw new BusinessException(SysUserEnum.ASSIGN_ROLE_FAILED);
            }
        }
        if (!rolesToRemove.isEmpty()) {
            int result = sysUserRoleMapper.removeUserRoles(userId, rolesToRemove);
            if (result <= 0) {
                throw new BusinessException(SysUserEnum.ASSIGN_ROLE_FAILED);
            }
        }

        // 在角色分配完成后，清除该用户的权限缓存
        permissionService.clearUserPermissionCache(userId);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysRoleVO> getUserRoles(UUID userId) {
        if (userId == null || this.getById(userId) == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        return sysUserRoleMapper.getUserRoles(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysPermissionVO> getUserPermissions(UUID userId) {
        if (userId == null || this.getById(userId) == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        return sysUserPermissionMapper.getUserPermissions(userId);
    }


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!StringUtils.hasText(username)) {
            throw new UsernameNotFoundException(SysUserEnum.USERNAME_CANNOT_BE_EMPTY.getMessage());
        }

        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException(SysUserEnum.USER_NOT_FOUND.getMessage());
        }

        List<SysRoleVO> roles = sysUserRoleMapper.getUserRoles(sysUser.getId());
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleKey()))
                .toList();

        return new User(sysUser.getUsername(), sysUser.getPassword(), authorities);
    }

    public SysUser checkSysUserInfo(Object sysUserInfo) {
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
        sysUser.setCreateTime(LocalDateTime.now());
        sysUser.setUpdateTime(LocalDateTime.now());
        sysUser.setStatus(DeletedEnum.NOT_DELETED.getCode());
        return sysUser;
    }
}
