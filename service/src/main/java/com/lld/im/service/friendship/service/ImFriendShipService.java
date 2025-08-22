package com.lld.im.service.friendship.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.RequestBase;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.friendship.model.req.*;

import java.util.List;

/**
 * ClassName: ImFriendService
 * Package: com.lld.im.service.friendship.service
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/19 上午10:20
 * Version 1.0
 */
public interface ImFriendShipService {
    public ResponseVO importFriendShip(ImportFriendShipReq req);
    public ResponseVO updateFriend(UpdateFriendReq req);
    public ResponseVO addFriend(AddFriendReq req);
    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId);
    public ResponseVO deleteFriend(DeleteFriendReq req);
    public ResponseVO deleteAllFriend(DeleteFriendReq req);
    public ResponseVO getAllFriendShip(GetAllFriendShipReq req);
    public ResponseVO getRelation(GetRelationReq req);
    public ResponseVO checkFriendship(CheckFriendShipReq req);
    public ResponseVO addBlack(AddFriendShipBlackReq req);
    public ResponseVO deleteBlack(DeleteBlackReq req);
    public ResponseVO checkBlck(CheckFriendShipReq req);
    public ResponseVO syncFriendshipList(SyncReq req);
    public List<String> getAllFriendId(String userId, Integer appId);
}
