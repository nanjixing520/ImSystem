package com.lld.im.service.friendship.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReadFriendShipRequestReq extends RequestBase {
    @NotBlank(message = "用户id不能为空")
    private String fromId;
}

