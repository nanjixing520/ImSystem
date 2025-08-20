package com.lld.message.model;

import com.lld.im.common.model.message.GroupChatMessageContent;
import com.lld.message.dao.ImMessageBodyEntity;
import lombok.Data;

/**
 * ClassName: DoStoreGroupMessageDto
 * Package: com.lld.message.model
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/18 上午11:26
 * Version 1.0
 */
@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
