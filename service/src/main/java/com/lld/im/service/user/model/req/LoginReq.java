package com.lld.im.service.user.model.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * ClassName: LoginReq
 * Package: com.lld.im.service.user.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/4 下午3:54
 * Version 1.0
 */
@Data
public class LoginReq {
    @NotNull(message = "用户id不能位空")
    private String userId;

    @NotNull(message = "appId不能为空")
    private Integer appId;

    private Integer clientType;
}
