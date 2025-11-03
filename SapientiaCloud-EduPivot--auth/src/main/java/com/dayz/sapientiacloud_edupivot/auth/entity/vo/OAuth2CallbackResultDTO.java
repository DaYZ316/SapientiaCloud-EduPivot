package com.dayz.sapientiacloud_edupivot.auth.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * OAuth2回调结果DTO
 * 用于封装OAuth2回调处理后的返回数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2回调结果")
public class OAuth2CallbackResultDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "用户信息")
    private SysUserInternalVO user;

    @Schema(description = "注册状态信息")
    private RegistrationStatusDTO registrationStatus;

    /**
     * 注册状态DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "注册状态信息")
    public static class RegistrationStatusDTO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

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
}

