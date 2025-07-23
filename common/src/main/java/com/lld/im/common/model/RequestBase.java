package com.lld.im.common.model;

import lombok.Data;

/**
 * ClassName: RequestBase
 * Package: com.lld.im.common.model
 * Description:
 *   公共的请求类
 * @Author 南极星
 * @Create 2025/7/17 下午4:48
 * Version 1.0
 */
@Data
public class RequestBase {
    /**
     * 接口供不同的应用去使用，需用appId区分应用的类型
     */
    private Integer appId;
    /**
     * 操作员(判断自己是不是收到审批的对象，看自己有没有审批的资格)
     */
    private String operater;
}
