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
import com.dayz.sapientiacloud_edupivot.system.entity.vo.*;
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
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService, UserDetailsService {

    private final static int DEFAULT_USERNAME_LENGTH = 8;
    private final static String INIT_PASSWORD = "123456";
    private final static String ADMIN = "ADMIN";
    private final static String STUDENT = "STUDENT";
    private final static String TEACHER = "TEACHER";

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final SysUserPermissionMapper sysUserPermissionMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PermissionService permissionService;

    @Override
    public PageInfo<SysUserVO> listSysUser(SysUserQueryDTO sysUserQueryDTO) {
        if (sysUserQueryDTO == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        return PageHelper.startPage(sysUserQueryDTO.getPageNum(), sysUserQueryDTO.getPageSize())
                .doSelectPageInfo(() -> sysUserMapper.listSysUser(sysUserQueryDTO));
    }

    @Override
    @Cacheable(value = "SysUser", key = "'all'", condition = "true")
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
        // 注意：验证码校验已在 auth 模块的 Controller 层完成，此处不再重复校验

        if (sysUserRegisterDTO == null || !StringUtils.hasText(sysUserRegisterDTO.getUsername())) {
            throw new BusinessException(SysUserEnum.USERNAME_CANNOT_BE_EMPTY);
        }
        if (sysUserMapper.selectByUsername(sysUserRegisterDTO.getUsername()) != null) {
            throw new BusinessException(SysUserEnum.USERNAME_ALREADY_EXISTS);
        }

        if (!StringUtils.hasText(sysUserRegisterDTO.getMobile())) {
            throw new BusinessException(SysUserEnum.MOBILE_CANNOT_BE_EMPTY);
        }

        if (isMobileExists(sysUserRegisterDTO.getMobile())) {
            throw new BusinessException(SysUserEnum.PHONE_NUMBER_ALREADY_EXISTS);
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

        if (!StringUtils.hasText(sysUserPasswordDTO.getNewPassword())) {
            throw new BusinessException(SysUserEnum.PASSWORD_CANNOT_BE_EMPTY);
        }

        SysUserInternalVO currentUser = UserContextUtil.getCurrentUser();

        currentUser.setPassword(passwordEncoder.encode(sysUserPasswordDTO.getNewPassword()));
        currentUser.setUpdateTime(LocalDateTime.now());

        return this.updateById(currentUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "SysUser", key = "#p0", condition = "#p0 != null"),
            @CacheEvict(value = "SysUser", key = "'all'", condition = "true")
    })
    public Boolean updatePasswordByUserId(UUID userId, String newPassword) {
        if (userId == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException(SysUserEnum.PASSWORD_CANNOT_BE_EMPTY);
        }

        SysUser sysUser = this.getById(userId);
        if (sysUser == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        sysUser.setPassword(passwordEncoder.encode(newPassword));
        sysUser.setUpdateTime(LocalDateTime.now());

        return this.updateById(sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "SysUser", key = "#p0", condition = "#p0 != null"),
            @CacheEvict(value = "SysUser", key = "'all'", condition = "true")
    })
    public Boolean resetPassword(UUID userId) {
        if (userId == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUser sysUser = this.getById(userId);
        if (sysUser == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        // 禁止重置 admin 用户的密码
        List<SysRoleVO> roles = sysUserRoleMapper.getUserRoles(userId);
        roles.forEach(role -> {
            if (role.isAdmin()) {
                throw new BusinessException(SysUserEnum.ADMIN_OPERATION_FORBIDDEN);
            }
        });

        // 重置密码为123456
        if (!StringUtils.hasText(INIT_PASSWORD)) {
            throw new BusinessException(SysUserEnum.PASSWORD_CANNOT_BE_EMPTY);
        }
        sysUser.setPassword(passwordEncoder.encode(INIT_PASSWORD));
        sysUser.setUpdateTime(LocalDateTime.now());

        return this.updateById(sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "SysUser", key = "#p0.id", condition = "#p0.id != null"),
            @CacheEvict(value = "SysUser", key = "'all'", condition = "true")
    })
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
    @Caching(evict = {
            @CacheEvict(value = "SysUser", key = "#p0", condition = "#p0 != null"),
            @CacheEvict(value = "SysUser", key = "'all'", condition = "true")
    })
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
    @Transactional(readOnly = true)
    public SysUserInternalVO getUserInfoById(UUID userId) {
        if (userId == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUser sysUser = this.getById(userId);
        if (sysUser == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUserInternalVO sysUserInternalVO = new SysUserInternalVO();
        BeanUtils.copyProperties(sysUser, sysUserInternalVO);

        sysUserInternalVO.setRoles(sysUserRoleMapper.getUserRoles(userId));
        sysUserInternalVO.setPermissions(sysUserPermissionMapper.getUserPermissions(userId));

        return sysUserInternalVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "SysUser", key = "#p0", condition = "#p0 != null")
    public Boolean assignRoles(UUID userId, List<UUID> newRoleIds) {
        if (userId == null || this.getById(userId) == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        if (newRoleIds == null) {
            throw new BusinessException(SysRoleEnum.ROLE_NOT_FOUND);
        }

        // 1. 获取现有角色和新角色的ID集合
        List<UUID> existingRoleIds = sysUserRoleMapper.getUserRoleIds(userId);

        // 创建Set
        Set<UUID> newRoleSet = new HashSet<>(newRoleIds);
        Set<UUID> existingRoleSet = new HashSet<>(existingRoleIds);

        // 2. 查询出学生和老师角色的ID，并存入Set以备后用
        List<String> specialRoleKeys = Arrays.asList(STUDENT, TEACHER);
        LambdaQueryWrapper<SysRole> specialRoleWrapper = new LambdaQueryWrapper<>();
        specialRoleWrapper.in(SysRole::getRoleKey, specialRoleKeys);
        Set<UUID> specialRoleIds = sysRoleMapper.selectList(specialRoleWrapper).stream()
                .map(SysRole::getId)
                .collect(Collectors.toSet());

        // 3. 新增角色中是否同时存在角色和老师角色，如果是则抛出异常
        if (newRoleSet.containsAll(specialRoleIds)) {
            throw new BusinessException(SysRoleEnum.STUDENT_AND_TEACHER_ROLE_FORBIDDEN_AT_SAME_TIME);
        }

        // 4. 检查现有角色是否包含管理员角色
        boolean hasAdminRole = false;
        if (!existingRoleSet.isEmpty()) {
            List<SysRoleVO> existingRoles = sysRoleMapper.getRolesByIds(existingRoleIds);
            hasAdminRole = existingRoles.stream().anyMatch(SysRoleVO::isAdmin);
        }

        // 5. 检查新增角色中是否包含学生或老师角色
        boolean addsSpecialRole = false;
        if (!newRoleSet.isEmpty()) {
            addsSpecialRole = specialRoleIds.stream().anyMatch(newRoleSet::contains);
        }

        //6. 将业务逻辑清晰地写入条件判断
        if (hasAdminRole && !addsSpecialRole) {
            throw new BusinessException(SysUserEnum.ADMIN_OPERATION_FORBIDDEN);
        }

        // 7. 计算需要添加和删除的角色ID
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
        sysUser.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        return sysUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isMobileExists(String mobile) {
        if (!StringUtils.hasText(mobile)) {
            return false;
        }

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getMobile, mobile)
                .eq(SysUser::getDeleted, DeletedEnum.NOT_DELETED.getCode());

        return sysUserMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isUsernameAvailable(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }

        if (username.length() < 4 || username.length() > 20) {
            return false;
        }

        SysUser existingUser = sysUserMapper.selectByUsername(username);

        return existingUser == null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "SysUser", key = "#result.user.id", condition = "#result != null && #result.user != null")
    public ThirdPartyLoginResultVO findOrCreateByThirdParty(String provider, String providerId, String username, String email, String name, String avatarUrl) {
        if (!StringUtils.hasText(provider) || !StringUtils.hasText(providerId) || !StringUtils.hasText(username)) {
            throw new BusinessException(SysUserEnum.USERNAME_CANNOT_BE_EMPTY);
        }

        if (!isSupportedProvider(provider)) {
            throw new BusinessException(SysUserEnum.THIRD_PARTY_PROVIDER_NOT_SUPPORTED);
        }

        SysUser existingUser = sysUserMapper.selectByThirdPartyId(provider, providerId);

        if (existingUser != null) {
//            boolean needUpdate = false;
//            if (StringUtils.hasText(email) && !Objects.equals(existingUser.getEmail(), email)) {
//                existingUser.setEmail(email);
//                needUpdate = true;
//            }
//            if (StringUtils.hasText(name) && !Objects.equals(existingUser.getNickName(), name)) {
//                existingUser.setNickName(name);
//                needUpdate = true;
//            }
//            if (StringUtils.hasText(avatarUrl) && !Objects.equals(existingUser.getAvatar(), avatarUrl)) {
//                existingUser.setAvatar(avatarUrl);
//                needUpdate = true;
//            }
//            if (StringUtils.hasText(username) && !Objects.equals(existingUser.getUsername(), username)) {
//                SysUser userByUsername = sysUserMapper.selectByUsername(username);
//                if (userByUsername == null || userByUsername.getId().equals(existingUser.getId())) {
//                    existingUser.setUsername(username);
//                    needUpdate = true;
//                }
//            }
//
//            if (!hasThirdPartyId(existingUser, provider, providerId)) {
//                setThirdPartyId(existingUser, provider, providerId);
//                needUpdate = true;
//            }
//
//            if (needUpdate) {
//                existingUser.setUpdateTime(LocalDateTime.now());
//                this.updateById(existingUser);
//            }
            return buildThirdPartyLoginResult(existingUser, false);
        }


        String finalUsername = username;
        SysUser userByUsername = sysUserMapper.selectByUsername(username);
        if (userByUsername != null) {
            String hash = md5Hex(provider + "_" + providerId);
            String suffix = provider.toLowerCase() + "_" + hash.substring(0, 8);
            finalUsername = username + "_" + suffix;

            // 确保生成的用户名唯一（如果还冲突，继续生成）
            int attempt = 1;
            while (sysUserMapper.selectByUsername(finalUsername) != null) {
                // 如果哈希值冲突，使用带计数器的哈希
                String hashWithCounter = md5Hex(provider + "_" + providerId + "_" + attempt);
                suffix = provider.toLowerCase() + "_" + hashWithCounter.substring(0, 8);
                finalUsername = username + "_" + suffix;
                attempt++;
                // 防止无限循环
                if (attempt > 100) {
                    throw new BusinessException(SysUserEnum.USERNAME_GENERATION_FAILED);
                }
            }
        }

        SysUser newUser = new SysUser();
        newUser.setId(UuidCreator.getTimeOrderedEpoch());
        newUser.setUsername(finalUsername);
        newUser.setNickName(StringUtils.hasText(name) ? name : finalUsername);
        newUser.setEmail(email);
        newUser.setAvatar(avatarUrl);

        // 保存第三方平台ID
        setThirdPartyId(newUser, provider, providerId);

        // 设置默认密码（第三方登录用户）
        newUser.setPassword(passwordEncoder.encode("THIRD_PARTY_USER_" + providerId));

        // 设置用户状态
        newUser.setStatus(StatusEnum.NORMAL.getCode());
        newUser.setGender(GenderEnum.UNKNOWN.getCode());
        newUser.setCreateTime(LocalDateTime.now());
        newUser.setUpdateTime(LocalDateTime.now());
        newUser.setDeleted(DeletedEnum.NOT_DELETED.getCode());

        // 保存用户
        boolean saveResult = this.save(newUser);
        if (!saveResult) {
            throw new BusinessException("创建第三方用户失败");
        }

        // 构建返回结果
        return buildThirdPartyLoginResult(newUser, true);
    }

    /**
     * 构建第三方登录结果
     */
    private ThirdPartyLoginResultVO buildThirdPartyLoginResult(SysUser user, boolean isNewUser) {
        // 构建用户VO
        SysUserInternalVO userVO = new SysUserInternalVO();
        BeanUtils.copyProperties(user, userVO);

        // 获取用户角色
        List<SysRoleVO> roles = sysUserRoleMapper.getUserRoles(user.getId());
        if (roles == null) {
            roles = Collections.emptyList();
        }
        userVO.setRoles(roles);

        // 判断是否需要绑定手机号
        boolean needBindMobile = !StringUtils.hasText(user.getMobile());

        // 判断是否需要选择身份（检查是否同时有学生和教师角色，或者都没有）
        boolean hasStudentRole = roles.stream().anyMatch(role -> STUDENT.equals(role.getRoleKey()));
        boolean hasTeacherRole = roles.stream().anyMatch(role -> TEACHER.equals(role.getRoleKey()));
        // 对于新用户，如果只分配了学生角色，则认为已选择身份；否则需要选择
        boolean needSelectIdentity = !isNewUser && (!hasStudentRole && !hasTeacherRole);

        // 判断是否需要完善信息（检查基本信息是否完整）
        // TODO 这里可以根据实际需求调整判断逻辑
        boolean needCompleteInfo = false;

        // 确定当前步骤
        String currentStep;
        if (needBindMobile) {
            currentStep = "bindMobile";
        } else if (needSelectIdentity) {
            currentStep = "selectIdentity";
        } else if (needCompleteInfo) {
            currentStep = "completeInfo";
        } else {
            currentStep = "completed";
        }

        // 构建结果对象
        ThirdPartyLoginResultVO result = ThirdPartyLoginResultVO.builder()
                .user(userVO)
                .isNewUser(isNewUser)
                .needBindMobile(needBindMobile)
                .needSelectIdentity(needSelectIdentity)
                .needCompleteInfo(needCompleteInfo)
                .currentStep(currentStep)
                .build();

        return result;
    }

    /**
     * 检查第三方平台是否支持
     */
    private boolean isSupportedProvider(String provider) {
        if (!StringUtils.hasText(provider)) {
            return false;
        }
        String lowerProvider = provider.toLowerCase();
        return "github".equals(lowerProvider) || "wechat".equals(lowerProvider);
    }

    /**
     * 检查用户是否已有该第三方平台的ID
     */
    private boolean hasThirdPartyId(SysUser user, String provider, String providerId) {
        if (user == null || !StringUtils.hasText(provider) || !StringUtils.hasText(providerId)) {
            return false;
        }
        String lowerProvider = provider.toLowerCase();
        return switch (lowerProvider) {
            case "github" -> StringUtils.hasText(user.getGithubId()) && user.getGithubId().equals(providerId);
            case "wechat" -> StringUtils.hasText(user.getWechatId()) && user.getWechatId().equals(providerId);
            default -> false;
        };
    }

    /**
     * 设置第三方平台ID到用户对象
     */
    private void setThirdPartyId(SysUser user, String provider, String providerId) {
        if (user == null || !StringUtils.hasText(provider) || !StringUtils.hasText(providerId)) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }
        String lowerProvider = provider.toLowerCase();
        switch (lowerProvider) {
            case "github":
                user.setGithubId(providerId);
                break;
            case "wechat":
                user.setWechatId(providerId);
                break;
            default:
                throw new BusinessException(SysUserEnum.THIRD_PARTY_PROVIDER_NOT_SUPPORTED);
        }
    }

    /**
     * 为新用户分配学生角色
     */
    private void assignStudentRoleToUser(UUID userId) {
        try {
            // 查找学生角色
            LambdaQueryWrapper<SysRole> roleQuery = new LambdaQueryWrapper<>();
            roleQuery.eq(SysRole::getRoleKey, "STUDENT")
                    .eq(SysRole::getStatus, StatusEnum.NORMAL.getCode())
                    .eq(SysRole::getDeleted, DeletedEnum.NOT_DELETED.getCode());

            SysRole studentRole = sysRoleMapper.selectOne(roleQuery);
            if (studentRole != null) {
                // 分配角色给用户
                List<UUID> roleIds = List.of(studentRole.getId());
                sysUserRoleMapper.addUserRoles(userId, roleIds);
            }
        } catch (Exception e) {
            // 不抛出异常，避免影响用户创建流程
        }
    }

    private String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // MD5 是标准算法，不会抛出此异常
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public SysUserInternalVO mobileLogin(SysUserMobileLoginDTO mobileLoginDTO) {
        if (mobileLoginDTO == null || !StringUtils.hasText(mobileLoginDTO.getMobile())) {
            throw new BusinessException(SysUserEnum.MOBILE_CANNOT_BE_EMPTY);
        }

        // 注意：验证码校验已在 auth 模块完成，此处不再重复校验
        // 根据手机号查找用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getMobile, mobileLoginDTO.getMobile())
                .eq(SysUser::getDeleted, DeletedEnum.NOT_DELETED.getCode())
                .eq(SysUser::getStatus, StatusEnum.NORMAL.getCode());

        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
        if (sysUser == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        // 更新最后登录时间
        sysUser.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(sysUser);

        // 转换为内部VO
        SysUserInternalVO sysUserInternalVO = new SysUserInternalVO();
        BeanUtils.copyProperties(sysUser, sysUserInternalVO);

        sysUserInternalVO.setRoles(sysUserRoleMapper.getUserRoles(sysUser.getId()));
        sysUserInternalVO.setPermissions(sysUserPermissionMapper.getUserPermissions(sysUser.getId()));

        return sysUserInternalVO;
    }

    @Override
    @Transactional(readOnly = true)
    public SysUserBasicInfoVO getUserInfoByMobile(String mobile) {
        if (!StringUtils.hasText(mobile)) {
            throw new BusinessException(SysUserEnum.MOBILE_CANNOT_BE_EMPTY);
        }

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getMobile, mobile)
                .eq(SysUser::getDeleted, DeletedEnum.NOT_DELETED.getCode());

        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
        if (sysUser == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUserBasicInfoVO sysUserBasicInfoVO = new SysUserBasicInfoVO();
        sysUserBasicInfoVO.setId(sysUser.getId());
        sysUserBasicInfoVO.setUsername(sysUser.getUsername());
        sysUserBasicInfoVO.setNickName(sysUser.getNickName());
        sysUserBasicInfoVO.setAvatar(sysUser.getAvatar());
        sysUserBasicInfoVO.setCreateTime(sysUser.getCreateTime());

        return sysUserBasicInfoVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "SysUser", key = "#p0", condition = "#p0 != null")
    public Boolean softDeleteUserById(UUID userId) {
        if (userId == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUser sysUser = this.getById(userId);
        if (sysUser == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        List<SysRoleVO> roles = sysUserRoleMapper.getUserRoles(userId);
        roles.forEach(role -> {
            if (role.isAdmin()) {
                throw new BusinessException(SysUserEnum.ADMIN_OPERATION_FORBIDDEN);
            }
        });

        return this.removeById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "SysUser", key = "#p0", condition = "#p0 != null")
    public Boolean updateGithubId(UUID userId, String githubId) {
        if (userId == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUser sysUser = this.getById(userId);
        if (sysUser == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        sysUser.setGithubId(githubId);
        sysUser.setUpdateTime(LocalDateTime.now());

        return this.updateById(sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "SysUser", key = "#p0", condition = "#p0 != null"),
            @CacheEvict(value = "SysUser", key = "'all'", condition = "true")
    })
    public Boolean physicalDeleteUserById(UUID id) {
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

        int deleted = sysUserMapper.physicalDeleteById(id);
        if (deleted > 0) {
            sysUserRoleMapper.removeRolesByUserId(id);
        }

        return deleted > 0;
    }
}
