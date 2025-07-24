package com.dayz.sapientiacloud_edupivot.auth.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "统一API响应体")
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 118904097912975986L;

    @Schema(name = "success", description = "请求是否成功", example = "true")
    private boolean success;

    @Schema(name = "code", description = "业务状态码 (200表示成功)", example = "200")
    private int code;

    @Schema(name = "message", description = "响应消息", example = "操作成功")
    private String message;

    @Schema(name = "data", description = "响应数据体 (泛型)")
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(ResultEnum.SUCCESS.getCode());
        result.setMessage(ResultEnum.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail() {
        return fail(ResultEnum.FAIL.getMessage());
    }

    public static <T> Result<T> fail(String message) {
        return fail(ResultEnum.FAIL.getCode(), message);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail(ResultEnum resultEnum) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(resultEnum.getCode());
        result.setMessage(resultEnum.getMessage());
        return result;
    }
}