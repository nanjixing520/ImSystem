package com.lld.im.service.friendship.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.model.req.ApproverFriendRequestReq;
import com.lld.im.service.friendship.model.req.FriendDto;
import com.lld.im.service.friendship.model.req.ReadFriendShipRequestReq;

/**
 * ClassName: ImFriendShipRequestService
 * Package: com.lld.im.service.friendship.service
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/21 上午9:47
 * Version 1.0
 */
public interface ImFriendShipRequestService {
    public ResponseVO addFienshipRequest(String fromId, FriendDto dto, Integer appId);
    public ResponseVO approverFriendRequest(ApproverFriendRequestReq req);
    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req);
    public ResponseVO getFriendRequest(String fromId, Integer appId);
}
