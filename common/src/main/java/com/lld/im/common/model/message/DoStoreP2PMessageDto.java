package com.lld.im.common.model.message;

import lombok.Data;

/**
 * ClassName: DoStoreP2PMessageDto
 * Package: com.lld.im.common.model.message
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/18 上午9:37
 * Version 1.0
 */
@Data
public class DoStoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBody messageBody;

}
