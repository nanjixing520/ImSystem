package com.lld.im.service.user.service;

import com.lld.im.service.user.model.UserStatusChangeNotifyContent;
import com.lld.im.service.user.model.req.PullFriendOnlineStatusReq;
import com.lld.im.service.user.model.req.PullUserOnlineStatusReq;
import com.lld.im.service.user.model.req.SetUserCustomerStatusReq;
import com.lld.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.lld.im.service.user.model.resp.UserOnlineStatusResp;

import java.util.Map;

/**
 * ClassName: ImUserStatusService
 * Package: com.lld.im.service.user.service
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/22 下午8:51
 * Version 1.0
 */
public interface ImUserStatusService {
    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    public void setUserCustomerStatus(SetUserCustomerStatusReq req);
    Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req);

    Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req);
}
