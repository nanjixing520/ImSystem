package com.lld.im.common.model.message;

import com.lld.im.common.model.ClientInfo;
import lombok.Data;

/**
 * ClassName: MessageReadedContent
 * Package: com.lld.im.common.model.message
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/20 上午8:59
 * Version 1.0
 */
@Data
public class MessageReadedContent extends ClientInfo {
    //消息序列号，标识我们读到了哪一条消息
    private long messageSequence;

    private String fromId;

    private String groupId;

    private String toId;
    //会话类型
    private Integer conversationType;

}
