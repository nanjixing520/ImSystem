package com.lld.im.common.model.message;

import lombok.Data;

/**
 * ClassName: CheckSendMessageReq
 * Package: com.lld.im.common.model.message
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/15 下午11:29
 * Version 1.0
 */
@Data
public class CheckSendMessageReq {

    private String fromId;

    private String toId;

    private Integer appId;

    private Integer command;

}
