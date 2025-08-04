package com.lld.im.common.model;

import lombok.Data;

/**
 * ClassName: UserSession
 * Package: com.lld.im.common.model
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/30 上午10:51
 * Version 1.0
 */
@Data
public class UserSession {

    private String userId;

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 端的标识
     */
    private Integer clientType;

    /**
     * sdk 版本号
     */
    private Integer version;

    /**
     * 连接状态  1=在线  2=离线
     */
    private Integer connectState;
    /**
     * 服务器Id
     */
    private Integer brokerId;
    /**
     * 服务器Ip地址
     */
    private String brokerHost;
}
