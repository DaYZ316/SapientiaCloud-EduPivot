package com.dayz.sapientiacloud_edupivot.auth.entity.dto;

import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserBasicInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "绑定手机号结果响应")
public class BindMobileResultDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4477631762355442285L;

    @Schema(name = "success", description = "是否成功")
    private Boolean success;

    @Schema(name = "needConfirm", description = "是否需要确认")
    private Boolean needConfirm;

    @Schema(name = "existingUserInfo", description = "已存在的用户基本信息（当needConfirm为true时返回）")
    private SysUserBasicInfoVO existingUserInfo;
}

