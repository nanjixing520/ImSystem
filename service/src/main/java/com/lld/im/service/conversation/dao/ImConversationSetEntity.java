package com.lld.im.service.conversation.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * ClassName: ImConversationSetEntity
 * Package: com.lld.im.service.conversation
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/20 上午10:13
 * Version 1.0
 */
@Data
@TableName("im_conversation_set")
public class ImConversationSetEntity {

    //会话id 会话类型_fromId_toId
    private String conversationId;

    //会话类型 0单聊1群聊
    private Integer conversationType;

    private String fromId;

    private String toId;
    //是否免打扰
    private int isMute;
    //是否置顶
    private int isTop;

    private Long sequence;
    //当前已读到的消息序列号
    private Long readedSequence;

    private Integer appId;
}

