package com.dayz.sapientiacloud_edupivot.system.common.exception;

import com.dayz.sapientiacloud_edupivot.system.common.result.ResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 221477430191313484L;

    private int code;

    private String message;

    public BusinessException(String message) {
        super(message);
        this.message = message;
        this.code = ResultEnum.FAIL.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultEnum resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
} 