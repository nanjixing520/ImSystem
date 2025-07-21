package com.lld.im.service.friendship.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * ClassName: GetFriendShipRequestReq
 * Package: com.lld.im.service.friendship.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/21 下午4:57
 * Version 1.0
 */
@Data
public class GetFriendShipRequestReq extends RequestBase {
    @NotBlank(message = "用户id不能为空")
    private String fromId;
}
