package com.lld.im.service.friendship.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.lld.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.lld.im.service.friendship.model.req.DeleteFriendShipGroupReq;
import org.springframework.stereotype.Service;

/**
 * ClassName: ImFriendShipGroupService
 * Package: com.lld.im.service.friendship.service
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/22 下午2:58
 * Version 1.0
 */

public interface ImFriendShipGroupService {
    public ResponseVO addGroup(AddFriendShipGroupReq req);

    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req);
    public ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId);
}
