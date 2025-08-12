package com.lld.im.service.friendship.model.callback;

import lombok.Data;

/**
 * @description:
 * @author: 南极星
 * @version: 1.0
 */
@Data
public class DeleteFriendAfterCallbackDto {

    private String fromId;

    private String toId;
}
