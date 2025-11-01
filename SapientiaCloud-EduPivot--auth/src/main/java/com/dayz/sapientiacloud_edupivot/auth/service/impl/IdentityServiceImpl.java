package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.clients.StudentClient;
import com.dayz.sapientiacloud_edupivot.auth.clients.TeacherClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SelectIdentityDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.StudentAddDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.TeacherAddDTO;
import com.dayz.sapientiacloud_edupivot.auth.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.auth.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.auth.service.IdentityService;
import com.dayz.sapientiacloud_edupivot.auth.utils.EnumUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 身份选择服务实现类
 */
@Service
@RequiredArgsConstructor
public class IdentityServiceImpl implements IdentityService {

    private final StudentClient studentClient;
    private final TeacherClient teacherClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean selectIdentity(SelectIdentityDTO selectIdentityDTO) {
        // 参数校验
        if (selectIdentityDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }
        
        // 安全获取 identityType 并校验
        String identityTypeStr = selectIdentityDTO.getIdentityType();
        if (!StringUtils.hasText(identityTypeStr)) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }
        String identityType = identityTypeStr.toLowerCase();

        // 获取当前登录用户ID
        java.util.UUID currentUserId = UserContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        Result<Boolean> result;

        if ("student".equals(identityType)) {
            // 校验学生信息
            if (selectIdentityDTO.getStudentInfo() == null) {
                throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
            }

            StudentAddDTO studentAddDTO = selectIdentityDTO.getStudentInfo();
            // 设置系统用户ID
            studentAddDTO.setSysUserId(currentUserId);

            // 调用学生模块创建学生记录
            result = studentClient.addStudent(studentAddDTO);
            if (result == null || !result.isSuccess()) {
                // 尝试根据返回的错误信息匹配对应的错误枚举（参考 register 方法的智能错误匹配）
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
                // 如果无法匹配具体错误，则抛出通用错误但携带详细信息
                String errorMsg = result != null && StringUtils.hasText(result.getMessage())
                    ? "创建学生记录失败: " + result.getMessage()
                    : SysUserEnum.USER_SERVICE_ERROR.getMessage();
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR.getCode(), errorMsg);
            }

            Boolean addResult = result.getData();
            if (addResult == null || !addResult) {
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
            }

            return addResult;

        } else if ("teacher".equals(identityType)) {
            // 校验教师信息
            if (selectIdentityDTO.getTeacherInfo() == null) {
                throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
            }

            TeacherAddDTO teacherAddDTO = selectIdentityDTO.getTeacherInfo();
            // 设置系统用户ID
            teacherAddDTO.setSysUserId(currentUserId);

            // 调用教师模块创建教师记录
            result = teacherClient.addTeacher(teacherAddDTO);
            if (result == null || !result.isSuccess()) {
                // 尝试根据返回的错误信息匹配对应的错误枚举（参考 register 方法的智能错误匹配）
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
                // 如果无法匹配具体错误，则抛出通用错误但携带详细信息
                String errorMsg = result != null && StringUtils.hasText(result.getMessage())
                    ? "创建教师记录失败: " + result.getMessage()
                    : SysUserEnum.USER_SERVICE_ERROR.getMessage();
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR.getCode(), errorMsg);
            }

            Boolean addResult = result.getData();
            if (addResult == null || !addResult) {
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
            }

            return addResult;

        } else {
            throw new BusinessException(ResultEnum.PARAM_ERROR.getCode(), "身份类型不正确，只能选择 student 或 teacher");
        }
    }
}

