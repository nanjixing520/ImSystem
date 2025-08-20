package com.lld.im.codec.pack.conversation;

import lombok.Data;

/**
 * ClassName: UpdateConversationPack
 * Package: com.lld.im.codec.pack.conversation
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/20 下午8:33
 * Version 1.0
 */
@Data
public class UpdateConversationPack {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private Integer conversationType;

    private Long sequence;

}
