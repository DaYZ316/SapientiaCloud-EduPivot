package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.clients.StudentClient;
import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.clients.TeacherClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SelectIdentityDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.StudentAddDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.TeacherAddDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserLoginVO;
import com.dayz.sapientiacloud_edupivot.auth.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.auth.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.security.utils.JwtUtil;
import com.dayz.sapientiacloud_edupivot.auth.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.auth.service.IdentityService;
import com.dayz.sapientiacloud_edupivot.auth.utils.EnumUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 身份选择服务实现类
 */
@Service
@RequiredArgsConstructor
public class IdentityServiceImpl implements IdentityService {

    private final StudentClient studentClient;
    private final TeacherClient teacherClient;
    private final SysUserClient sysUserClient;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserLoginVO selectIdentity(SelectIdentityDTO selectIdentityDTO) {
        if (selectIdentityDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }

        String identityTypeStr = selectIdentityDTO.getIdentityType();
        if (!StringUtils.hasText(identityTypeStr)) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }
        String identityType = identityTypeStr.toLowerCase();

        UUID currentUserId = UserContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        Result<Boolean> result;

        if ("student".equals(identityType)) {
            if (selectIdentityDTO.getStudentInfo() == null) {
                throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
            }

            StudentAddDTO studentAddDTO = selectIdentityDTO.getStudentInfo();
            studentAddDTO.setSysUserId(currentUserId);

            result = studentClient.addStudent(studentAddDTO);
            if (result == null || !result.isSuccess()) {
                if (result != null && StringUtils.hasText(result.getMessage())) {
                    SysUserEnum sysUserEnum = EnumUtil.getByAttribute(
                        SysUserEnum.class,
                        result.getMessage(),
                        SysUserEnum::getMessage
                    );
                    if (sysUserEnum != null) {
                        throw new BusinessException(sysUserEnum);
                    }
                }
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
            }

            Boolean addResult = result.getData();
            if (addResult == null || !addResult) {
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
            }

        } else if ("teacher".equals(identityType)) {
            if (selectIdentityDTO.getTeacherInfo() == null) {
                throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
            }

            TeacherAddDTO teacherAddDTO = selectIdentityDTO.getTeacherInfo();
            teacherAddDTO.setSysUserId(currentUserId);

            result = teacherClient.addTeacher(teacherAddDTO);
            if (result == null || !result.isSuccess()) {
                if (result != null && StringUtils.hasText(result.getMessage())) {
                    SysUserEnum sysUserEnum = EnumUtil.getByAttribute(
                        SysUserEnum.class,
                        result.getMessage(),
                        SysUserEnum::getMessage
                    );
                    if (sysUserEnum != null) {
                        throw new BusinessException(sysUserEnum);
                    }
                }
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
            }

            Boolean addResult = result.getData();
            if (addResult == null || !addResult) {
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
            }

        } else {
            throw new BusinessException(ResultEnum.PARAM_ERROR.getCode(), "身份类型不正确，只能选择 student 或 teacher");
        }

        Result<SysUserInternalVO> userResult = sysUserClient.getUserInfoById(currentUserId);
        if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUserInternalVO userVO = userResult.getData();
        String token = jwtUtil.generateToken(userVO);

        SysUserLoginVO loginVO = new SysUserLoginVO();
        loginVO.setAccessToken(token);
        BeanUtils.copyProperties(userVO, loginVO);
        // 在返回之前将密码字段置空，避免密码泄露
        loginVO.setPassword(null);

        return loginVO;
    }
}

