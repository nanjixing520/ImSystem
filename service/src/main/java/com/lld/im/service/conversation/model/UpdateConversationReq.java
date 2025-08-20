package com.lld.im.service.conversation.model;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * ClassName: UpdateConversationReq
 * Package: com.lld.im.service.conversation.model
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/20 下午8:36
 * Version 1.0
 */
@Data
public class UpdateConversationReq extends RequestBase {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private String fromId;


}