package com.lld.im.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: ClientInfo
 * Package: com.lld.im.common.model
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/6 下午9:25
 * Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo {
    private Integer appId;
    private Integer clientType;
    private String imei;
}
