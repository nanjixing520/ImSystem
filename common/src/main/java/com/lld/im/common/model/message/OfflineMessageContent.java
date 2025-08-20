package com.lld.im.common.model.message;

import lombok.Data;

/**
 * ClassName: OfflineMessageContent
 * Package: com.lld.im.common.model.message
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/21 上午12:28
 * Version 1.0
 */
@Data
public class OfflineMessageContent {

    private Integer appId;

    /** messageBodyId*/
    private Long messageKey;

    /** messageBody*/
    private String messageBody;

    private Long messageTime;

    private String extra;

    private Integer delFlag;

    private String fromId;

    private String toId;

    /** 序列号*/
    private Long messageSequence;

    private String messageRandom;

    private Integer conversationType;

    private String conversationId;

}

