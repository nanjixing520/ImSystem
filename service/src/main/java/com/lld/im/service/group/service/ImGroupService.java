package com.lld.im.service.group.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.group.model.req.*;

import javax.annotation.Resource;

/**
 * ClassName: ImGroupService
 * Package: com.lld.im.service.group.service
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/22 下午5:37
 * Version 1.0
 */
public interface ImGroupService {
    public ResponseVO importGroup(ImportGroupReq req);
    public ResponseVO createGroup(CreateGroupReq req);
    public ResponseVO<ImGroupEntity> getGroup(GetGroupReq req);
    public ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId);
    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req);
    public ResponseVO getJoinedGroup(GetJoinedGroupReq req);
    public ResponseVO destroyGroup(DestroyGroupReq req);
    public ResponseVO transferGroup(TransferGroupReq req);
    public ResponseVO muteGroup(MuteGroupReq req);
}
