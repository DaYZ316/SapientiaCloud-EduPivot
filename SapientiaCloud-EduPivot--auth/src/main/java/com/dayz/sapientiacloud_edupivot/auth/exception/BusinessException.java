package com.dayz.sapientiacloud_edupivot.auth.exception;

import com.dayz.sapientiacloud_edupivot.auth.enums.BaseEnum;
import com.dayz.sapientiacloud_edupivot.auth.enums.ResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    @Serial
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

    public <E extends Enum<E> & BaseEnum> BusinessException(E baseEnum) {
        super(baseEnum.getMessage());
        this.code = baseEnum.getCode();
        this.message = baseEnum.getMessage();
    }
} 