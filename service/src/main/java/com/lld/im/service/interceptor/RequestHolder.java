package com.lld.im.service.interceptor;

/**
 * @author: 南极星
 * @description:
 **/
public class RequestHolder {
    // Java 中的一个线程本地变量，它的作用是为每个线程存储一个独立的 Boolean 类型的值，各个线程之间的变量互不干扰。
    private final static ThreadLocal<Boolean> requestHolder = new ThreadLocal<>();

    public static void set(Boolean isadmin) {
        requestHolder.set(isadmin);
    }

    public static Boolean get() {
        return requestHolder.get();
    }

    public static void remove() {
        requestHolder.remove();
    }
}
