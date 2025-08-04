package com.lld.im.common.model;

import lombok.Data;

/**
 * ClassName: UserClientDto
 * Package: com.lld.im.common.model
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/30 下午3:14
 * Version 1.0
 */
@Data
public class UserClientDto {
    private Integer appId;
    private Integer clientType;
    private String userId;
    private String imei;
}
