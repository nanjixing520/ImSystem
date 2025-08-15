package com.lld.im.codec.pack.message;

import lombok.Data;

/**
 * ClassName: ChatMessageAck
 * Package: com.lld.im.codec.pack.message
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/14 上午10:51
 * Version 1.0
 */
@Data
public class ChatMessageAck {

    private String messageId;
    private Long messageSequence;

    public ChatMessageAck(String messageId) {
        this.messageId = messageId;
    }

    public ChatMessageAck(String messageId,Long messageSequence) {
        this.messageId = messageId;
        this.messageSequence = messageSequence;
    }

}
