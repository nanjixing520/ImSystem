package com.lld.message.model;

import com.lld.im.common.model.message.MessageContent;
import com.lld.message.dao.ImMessageBodyEntity;
import lombok.Data;

/**
 * ClassName: DoStoreP2PMessageDto
 * Package: com.lld.message.model
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/18 上午11:26
 * Version 1.0
 */
@Data
public class DoStoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
