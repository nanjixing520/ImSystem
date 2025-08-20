package com.lld.im.common.enums;

import com.lld.im.common.exception.ApplicationExceptionEnum;

public enum ConversationErrorCode implements ApplicationExceptionEnum {

    CONVERSATION_UPDATE_PARAM_ERROR(50000,"会话修改参数错误"),
    CONVERSATION_IS_NOT_EXIST(50001,"会话不存在"),


    ;

    private int code;
    private String error;

    ConversationErrorCode(int code, String error){
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