package com.lld.im.service.group.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.model.resp.GetRoleInGroupResp;

import java.util.Collection;
import java.util.List;

/**
 * ClassName: ImGroupMemberService
 * Package: com.lld.im.service.group.service
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/22 下午5:37
 * Version 1.0
 */
public interface ImGroupMemberService {
    public ResponseVO importGroupMember(ImportGroupMemberReq req);
    public ResponseVO addMember(AddGroupMemberReq req);
    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto);

    /**
     * 获得成员在当前这个组的角色
     * @param groupId
     * @param memberId
     * @param appId
     * @return
     */
    public ResponseVO<GetRoleInGroupResp> getRoleInGroupOne(String groupId, String memberId, Integer appId);
    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId, Integer appId);

    /**
     * 获取成员加入到的所有的群id
     * @param req
     * @return
     */
    public ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req);

    public ResponseVO transferGroupMember(String ownerId, String groupId, Integer appId);
    public ResponseVO removeMember(RemoveGroupMemberReq req);
    public ResponseVO removeGroupMember(String groupId, Integer appId, String memberId);
    public ResponseVO exitGroup(ExitGroupReq req);

    public ResponseVO updateGroupMember(UpdateGroupMemberReq req);

    public ResponseVO speak(SpeakMemberReq req);
    public List<String> getGroupMemberId(String groupId, Integer appId);

    public List<GroupMemberDto> getGroupManager(String groupId, Integer appId);
}
