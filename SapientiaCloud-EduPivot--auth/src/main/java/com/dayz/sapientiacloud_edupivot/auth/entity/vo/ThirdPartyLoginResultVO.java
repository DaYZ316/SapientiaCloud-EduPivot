package com.dayz.sapientiacloud_edupivot.auth.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "第三方登录结果VO")
public class ThirdPartyLoginResultVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户信息")
    private SysUserInternalVO user;

    @Schema(description = "是否为新用户")
    private Boolean isNewUser;

    @Schema(description = "是否需要绑定手机号")
    private Boolean needBindMobile;

    @Schema(description = "是否需要选择身份")
    private Boolean needSelectIdentity;

    @Schema(description = "是否需要完善信息")
    private Boolean needCompleteInfo;

    @Schema(description = "当前步骤: bindMobile | selectIdentity | completeInfo | completed")
    private String currentStep;
}

