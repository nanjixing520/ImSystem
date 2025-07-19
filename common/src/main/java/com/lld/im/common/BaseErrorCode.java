package com.lld.im.common;

import com.lld.im.common.exception.ApplicationExceptionEnum;

/**
 * ClassName: BaseErrorCode
 * Package: com.lld.im.common
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/17 下午3:11
 * Version 1.0
 */
public enum BaseErrorCode implements ApplicationExceptionEnum {
    SUCCESS(200,"success"),
    SYSTEM_ERROR(90000,"服务器内部错误,请联系管理员"),
    PARAMETER_ERROR(90001,"参数校验错误"),
    ;

    private int code;
    private String error;

    BaseErrorCode(int code, String error){
        this.code = code;
        this.error = error;
    }
    public int getCode() {
        return this.code;
    }

    public String getError() {
        return this.error;
    }

}

