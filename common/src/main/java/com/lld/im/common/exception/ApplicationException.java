package com.lld.im.common.exception;

/**
 * ClassName: ApplicationException
 * Package: com.lld.im.common
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/17 下午3:02
 * Version 1.0
 */
public class ApplicationException extends RuntimeException{
    private int code;
    private String error;
    public ApplicationException(int code, String message) {
        super(message);
        this.code = code;
        this.error = message;
    }
    public ApplicationException(ApplicationExceptionEnum exceptionEnum) {
        super(exceptionEnum.getError());
        this.code   = exceptionEnum.getCode();
        this.error  = exceptionEnum.getError();
    }

    public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }
    /**
     *  avoid the expensive and useless stack trace for api exceptions
     *  @see Throwable#fillInStackTrace()
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
