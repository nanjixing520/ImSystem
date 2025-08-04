package com.lld.im.codec.proto;

import lombok.Data;

/**
 * ClassName: Message
 * Package: com.lld.im.codec.proto
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/28 下午9:54
 * Version 1.0
 */
@Data
public class Message {

    private MessageHeader messageHeader;

    private Object messagePack;

    @Override
    public String toString() {
        return "Message{" +
                "messageHeader=" + messageHeader +
                ", messagePack=" + messagePack +
                '}';

    }
}
