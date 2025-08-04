package com.lld.im.common;

/**
 * ClassName: ClientType
 * Package: com.lld.im.common
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/4 上午8:43
 * Version 1.0
 */
public enum ClientType {

    WEBAPI(0,"webApi"),
    WEB(1,"web"),
    IOS(2,"ios"),
    ANDROID(3,"android"),
    WINDOWS(4,"windows"),
    MAC(5,"mac"),
    ;

    private int code;
    private String error;

    ClientType(int code, String error){
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
