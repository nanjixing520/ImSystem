package com.lld.im.service.friendship.model.req;

import lombok.Data;

/**
 * ClassName: FriendDto
 * Package: com.lld.im.service.friendship.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/21 上午9:06
 * Version 1.0
 */
@Data
public class FriendDto {
    private String toId;

    private String remark;

    private String addSource;

    private String extra;

    private String addWording;
}
