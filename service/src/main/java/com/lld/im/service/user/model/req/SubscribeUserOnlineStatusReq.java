package com.lld.im.service.user.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

/**
 * ClassName: SubscribeUserOnlineStatusReq
 * Package: com.lld.im.service.user.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/22 下午9:52
 * Version 1.0
 */
@Data
public class SubscribeUserOnlineStatusReq extends RequestBase {
    //被订阅人的id
    private List<String> subUserId;
    //订阅过期时间
    private Long subTime;


}