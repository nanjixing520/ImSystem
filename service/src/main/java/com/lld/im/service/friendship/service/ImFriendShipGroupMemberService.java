package com.lld.im.service.friendship.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.lld.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;

/**
 * ClassName: ImFriendShipGroupMemberService
 * Package: com.lld.im.service.friendship.service
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/22 下午2:58
 * Version 1.0
 */
public interface ImFriendShipGroupMemberService {
    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req);

    public ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req);
    public int doAddGroupMember(Long groupId, String toId);

    public int clearGroupMember(Long groupId);
}
