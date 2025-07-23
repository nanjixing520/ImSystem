package com.lld.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.GroupErrorCode;
import com.lld.im.common.enums.GroupMemberRoleEnum;
import com.lld.im.common.enums.GroupStatusEnum;
import com.lld.im.common.enums.GroupTypeEnum;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMapper;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.model.resp.GetGroupResp;
import com.lld.im.service.group.model.resp.GetJoinedGroupResp;
import com.lld.im.service.group.model.resp.GetRoleInGroupResp;
import com.lld.im.service.group.service.ImGroupMemberService;
import com.lld.im.service.group.service.ImGroupService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * ClassName: ImGroupServiceImpl
 * Package: com.lld.im.service.group.service.impl
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/22 下午5:38
 * Version 1.0
 */
@Service
public class ImGroupServiceImpl implements ImGroupService {
    @Autowired
    private ImGroupMapper imGroupDataMapper;
    @Autowired
    private ImGroupMemberService groupMemberService;

    /**
     * 解散群组，只支持后台管理员和群主解散(私有群只能支持后台管理员删除，公开群都行)
     * 补充：增加群主删除公开群的逻辑（本身已经实现，开始没有完全理解代码）
     * @param req
     * @return
     */
    @Override
    @Transactional
    public ResponseVO destroyGroup(DestroyGroupReq req) {
        boolean isAdmin = false;
        QueryWrapper<ImGroupEntity> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("group_id", req.getGroupId());
        objectQueryWrapper.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupDataMapper.selectOne(objectQueryWrapper);
        if (imGroupEntity == null) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }

        if(imGroupEntity.getStatus() == GroupStatusEnum.DESTROY.getCode()){
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        if (!isAdmin) {
            if (imGroupEntity.getGroupType() == GroupTypeEnum.PRIVATE.getCode()) {
                throw new ApplicationException(GroupErrorCode.PRIVATE_GROUP_CAN_NOT_DESTORY);
            }
            if (imGroupEntity.getGroupType() == GroupTypeEnum.PUBLIC.getCode() &&
                    !imGroupEntity.getOwnerId().equals(req.getOperater())) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }
        }

        ImGroupEntity update = new ImGroupEntity();

        update.setStatus(GroupStatusEnum.DESTROY.getCode());
        int update1 = imGroupDataMapper.update(update, objectQueryWrapper);
        if (update1 != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }
        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO transferGroup(TransferGroupReq req) {
        //检查操作人是否在群内是否是群主
        ResponseVO<GetRoleInGroupResp> roleInGroupOne = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());
        if (!roleInGroupOne.isOk()) {
            return roleInGroupOne;
        }

        if (roleInGroupOne.getData().getRole() != GroupMemberRoleEnum.OWNER.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
        }
        //检查被移交群主的人是否在群内
        ResponseVO<GetRoleInGroupResp> newOwnerRole = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOwnerId(), req.getAppId());
        if (!newOwnerRole.isOk()) {
            return newOwnerRole;
        }

        QueryWrapper<ImGroupEntity> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("group_id", req.getGroupId());
        objectQueryWrapper.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupDataMapper.selectOne(objectQueryWrapper);
        if(imGroupEntity.getStatus() == GroupStatusEnum.DESTROY.getCode()){
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        ImGroupEntity updateGroup = new ImGroupEntity();
        updateGroup.setOwnerId(req.getOwnerId());
        UpdateWrapper<ImGroupEntity> updateGroupWrapper = new UpdateWrapper<>();
        updateGroupWrapper.eq("app_id", req.getAppId());
        updateGroupWrapper.eq("group_id", req.getGroupId());
        imGroupDataMapper.update(updateGroup, updateGroupWrapper);
        groupMemberService.transferGroupMember(req.getOwnerId(), req.getGroupId(), req.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO muteGroup(MuteGroupReq req) {

        ResponseVO<ImGroupEntity> groupResp = getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            return groupResp;
        }

        if(groupResp.getData().getStatus() == GroupStatusEnum.DESTROY.getCode()){
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        boolean isadmin = false;

        if (!isadmin) {
            //不是后台调用需要检查权限
            ResponseVO<GetRoleInGroupResp> role = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());

            if (!role.isOk()) {
                return role;
            }

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode() || roleInfo == GroupMemberRoleEnum.OWNER.getCode();

            //只有群主或管理员可以实现群禁言
            if (!isManager) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        ImGroupEntity update = new ImGroupEntity();
        update.setMute(req.getMute());

        UpdateWrapper<ImGroupEntity> wrapper = new UpdateWrapper<>();
        wrapper.eq("group_id",req.getGroupId());
        wrapper.eq("app_id",req.getAppId());
        imGroupDataMapper.update(update,wrapper);

        return ResponseVO.successResponse();
    }

    /**
     * 导入群组的时候公开群必须指定群组
     * @param req
     * @return
     */
    @Override
    public ResponseVO importGroup(ImportGroupReq req) {
        //如果请求中没有携带群组id那就设置一个，有的话查看数据库中是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        if(StringUtils.isEmpty(req.getGroupId())){
            req.setGroupId(UUID.randomUUID().toString().replace("-",""));
        }else{
            query.eq("group_id", req.getGroupId());
            query.eq("app_id", req.getAppId());
            Integer i = imGroupDataMapper.selectCount(query);
            if(i>0){
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }
        ImGroupEntity imGroupEntity = new ImGroupEntity();
        BeanUtils.copyProperties(req, imGroupEntity);
        //公开群必须指定群主
        if(req.getGroupType()== GroupTypeEnum.PUBLIC.getCode()&&StringUtils.isBlank(req.getOwnerId())){
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }
        if(req.getCreateTime()==null){
            imGroupEntity.setCreateTime(System.currentTimeMillis());
        }
        imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());
        int insert = imGroupDataMapper.insert(imGroupEntity);
        if(insert!=1){
            throw new ApplicationException(GroupErrorCode.IMPORT_GROUP_ERROR);
        }
        return ResponseVO.successResponse();
    }

    /**
     * 创建群组的时候可以导入群成员和群主
     * 公开群必须指定群主
     * TODO：对于已经删除的群聊进行判断以及更新
     * @param req
     * @return
     */
    @Override
    @Transactional
    public ResponseVO createGroup(CreateGroupReq req) {

        boolean isAdmin = false;

        if (!isAdmin) {
            req.setOwnerId(req.getOperater());
        }
        //1.判断群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            query.eq("group_id", req.getGroupId());
            query.eq("app_id", req.getAppId());
            Integer integer = imGroupDataMapper.selectCount(query);
            if (integer > 0) {
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }
        //公开群必须指定群主
        if (req.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())) {
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }
        //将群组信息入库
        ImGroupEntity imGroupEntity = new ImGroupEntity();
        imGroupEntity.setCreateTime(System.currentTimeMillis());
        imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(req, imGroupEntity);
        int insert = imGroupDataMapper.insert(imGroupEntity);
        //+判断群主id是否存在，存在入库群主信息
        if(req.getOwnerId()!=null){
            GroupMemberDto groupMemberDto = new GroupMemberDto();
            groupMemberDto.setMemberId(req.getOwnerId());
            groupMemberDto.setRole(GroupMemberRoleEnum.OWNER.getCode());
            groupMemberDto.setJoinTime(System.currentTimeMillis());
            groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), groupMemberDto);
        }
        if(req.getMember()!=null){
            //插入群成员
            for (GroupMemberDto dto : req.getMember()) {
                groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), dto);
            }
        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getGroup(GetGroupReq req) {

        ResponseVO group = this.getGroup(req.getGroupId(), req.getAppId());

        if(!group.isOk()){
            return group;
        }

        GetGroupResp getGroupResp = new GetGroupResp();
        BeanUtils.copyProperties(group.getData(), getGroupResp);
        //获取群成员信息
        try {
            ResponseVO<List<GroupMemberDto>> groupMember = groupMemberService.getGroupMember(req.getGroupId(), req.getAppId());
            if (groupMember.isOk()) {
                getGroupResp.setMemberList(groupMember.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseVO.successResponse(getGroupResp);
    }

    @Override
    public ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId) {
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("group_id", groupId);
        ImGroupEntity imGroupEntity = imGroupDataMapper.selectOne(query);

        if (imGroupEntity == null) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(imGroupEntity);
    }

    /**
     * @param
     * @return com.lld.im.common.ResponseVO
     * @description 修改群基础信息
     * 如果是后台管理员调用，则不检查权限，
     * 如果不是则检查权限，如果是私有群（微信群）任何人都可以修改资料，公开群只有群主管理员可以修改
     * 如果是群主或者管理员可以修改其他信息。
     * @author 南极星
     */
    @Override
    @Transactional
    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req) {

        //1.判断群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("group_id", req.getGroupId());
        query.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupDataMapper.selectOne(query);
        if (imGroupEntity == null) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }

        if(imGroupEntity.getStatus() == GroupStatusEnum.DESTROY.getCode()){
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        boolean isAdmin = false;

        if (!isAdmin) {
            //不是后台调用需要检查权限
            ResponseVO<GetRoleInGroupResp> role = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());

            if (!role.isOk()) {
                return role;
            }

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode() || roleInfo == GroupMemberRoleEnum.OWNER.getCode();

            //公开群只能群主管理员修改资料
            if (!isManager && GroupTypeEnum.PUBLIC.getCode() == imGroupEntity.getGroupType()) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }

        }

        ImGroupEntity update = new ImGroupEntity();
        BeanUtils.copyProperties(req, update);
        update.setUpdateTime(System.currentTimeMillis());
        int row = imGroupDataMapper.update(update, query);
        if (row != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }

        return ResponseVO.successResponse();
    }

    /**
     * @param
     * @return com.lld.im.common.ResponseVO
     * @description 获取用户加入的群组
     * @author 南极星
     */
    @Override
    public ResponseVO getJoinedGroup(GetJoinedGroupReq req) {
        //获取用户所有加入的群id
        ResponseVO<Collection<String>> memberJoinedGroup = groupMemberService.getMemberJoinedGroup(req);
        if (memberJoinedGroup.isOk()) {
            GetJoinedGroupResp resp = new GetJoinedGroupResp();
            //如果没有加入群组
            if (CollectionUtils.isEmpty(memberJoinedGroup.getData())) {
                resp.setTotalCount(0);
                resp.setGroupList(new ArrayList<>());
                return ResponseVO.successResponse(resp);
            }
            QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.in("group_id", memberJoinedGroup.getData());

            if (CollectionUtils.isNotEmpty(req.getGroupType())) {
                query.in("group_type", req.getGroupType());
            }
            List<ImGroupEntity> groupList = imGroupDataMapper.selectList(query);
            resp.setGroupList(groupList);
            if (req.getLimit() == null) {
                resp.setTotalCount(groupList.size());
            } else {
                resp.setTotalCount(imGroupDataMapper.selectCount(query));
            }
            return ResponseVO.successResponse(resp);
        } else {
            return memberJoinedGroup;
        }
    }
}
