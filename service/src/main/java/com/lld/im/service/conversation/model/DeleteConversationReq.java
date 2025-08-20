package com.lld.im.service.conversation.model;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * ClassName: DeleteConversationReq
 * Package: com.lld.im.service.conversation.model
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/20 下午8:35
 * Version 1.0
 */
@Data
public class DeleteConversationReq extends RequestBase {

    @NotBlank(message = "会话id不能为空")
    private String conversationId;

    @NotBlank(message = "fromId不能为空")
    private String fromId;

}
