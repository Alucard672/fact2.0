package com.garment.common.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author garment
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 参数错误异常
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    /**
     * 未授权异常
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }

    /**
     * 禁止访问异常
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    /**
     * 资源未找到异常
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    /**
     * 租户相关异常
     */
    public static BusinessException tenantError(String message) {
        return new BusinessException(4001, message);
    }

    /**
     * 权限相关异常
     */
    public static BusinessException permissionError(String message) {
        return new BusinessException(4003, message);
    }

    /**
     * 数据验证异常
     */
    public static BusinessException validationError(String message) {
        return new BusinessException(4004, message);
    }

    /**
     * 服务器内部错误
     */
    public static BusinessException error(String message) {
        return new BusinessException(500, message);
    }

    /**
     * 获取错误码
     */
    public int getCode() {
        return this.code;
    }

    /**
     * 获取错误消息
     */
    @Override
    public String getMessage() {
        return this.message;
    }
}