package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import com.dayz.sapientiacloud_edupivot.system.common.entity.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统用户分页查询参数")
public class SysUserQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -7909775432716685324L;

    /** 用户名称 */
    @Schema(description = "用户名")
    @Size(min = 0, max = 20, message = "如果提供用户名，其长度必须在0到20个字符之间")
    private String username;

    /** 用户昵称 */
    @Schema(description = "用户昵称")
    @Size(max = 30, message = "用户昵称不能超过30个字符")
    private String nickName;

    /** 用户性别 */
    @Schema(description = "性别 (0=未知, 1=男, 2=女)")
    @Min(value = 0, message = "性别输入不正确")
    @Max(value = 2, message = "性别输入不正确")
    private Integer gender;

    /** 用户状态 */
    @Schema(description = "状态 (0=正常, 1=停用)")
    @Min(value = 0, message = "状态输入不正确")
    @Max(value = 1, message = "状态输入不正确")
    private Integer status;
    
    /** 手机号码 */
    @Schema(description = "手机号码")
    private String mobile;
    
    /** 邮箱 */
    @Schema(description = "邮箱")
    private String email;
}
