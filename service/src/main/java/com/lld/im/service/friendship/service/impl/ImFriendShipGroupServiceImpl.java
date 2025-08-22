package com.lld.im.service.friendship.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.codec.pack.friendship.AddFriendGroupPack;
import com.lld.im.codec.pack.friendship.DeleteFriendGroupPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.enums.FriendShipErrorCode;
import com.lld.im.common.enums.command.FriendshipEventCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipGroupMapper;
import com.lld.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.lld.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.lld.im.service.friendship.model.req.DeleteFriendShipGroupReq;
import com.lld.im.service.friendship.service.ImFriendShipGroupMemberService;
import com.lld.im.service.friendship.service.ImFriendShipGroupService;
import com.lld.im.service.seq.RedisSeq;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.MessageProducer;
import com.lld.im.service.utils.WriteUserSeq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ClassName: ImFriendShipGroupServiceImpl
 * Package: com.lld.im.service.friendship.service.impl
 * Description:
 *    好友分组逻辑的处理  例如qq好友分组：亲人，朋友，同事
 * @Author 南极星
 * @Create 2025/7/22 下午3:00
 * Version 1.0
 */
@Service
public class ImFriendShipGroupServiceImpl implements ImFriendShipGroupService {
    @Autowired
    ImFriendShipGroupMapper imFriendShipGroupMapper;

    @Autowired
    ImFriendShipGroupMemberService imFriendShipGroupMemberService;

    @Autowired
    ImUserService imUserService;
    @Autowired
    MessageProducer messageProducer;
    @Autowired
    RedisSeq redisSeq;
    @Autowired
    WriteUserSeq writeUserSeq;

    @Override
    @Transactional
    public ResponseVO addGroup(AddFriendShipGroupReq req) {
        //查询数据库中是否有这条群组关系，有的话判断状态（删除状态则恢复更新创建时间添加成员，存在则返回已存在），没有的话添加群组关系
        QueryWrapper<ImFriendShipGroupEntity> query = new QueryWrapper<>();
        query.eq("group_name", req.getGroupName());
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        long seq=redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.FriendshipGroup+":"+req.getFromId());
        ImFriendShipGroupEntity entity = imFriendShipGroupMapper.selectOne(query);
        if (entity != null) {
            if(entity.getDelFlag()==DelFlagEnum.NORMAL.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
            }else{
                ImFriendShipGroupEntity insert = new ImFriendShipGroupEntity();
                insert.setSequence(seq);
                insert.setCreateTime(System.currentTimeMillis());
                insert.setDelFlag(DelFlagEnum.NORMAL.getCode());
                int i = imFriendShipGroupMapper.update(insert, query);
                if (i == 1 && CollectionUtil.isNotEmpty(req.getToIds())) {
                    AddFriendShipGroupMemberReq addFriendShipGroupMemberReq = new AddFriendShipGroupMemberReq();
                    addFriendShipGroupMemberReq.setFromId(req.getFromId());
                    addFriendShipGroupMemberReq.setGroupName(req.getGroupName());
                    addFriendShipGroupMemberReq.setToIds(req.getToIds());
                    addFriendShipGroupMemberReq.setAppId(req.getAppId());
                    imFriendShipGroupMemberService.addGroupMember(addFriendShipGroupMemberReq);
                    writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.FriendshipGroup, seq);
                    return ResponseVO.successResponse();
                }else{
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_CREATE_ERROR);
                }
            }
        }
        //写入db
        ImFriendShipGroupEntity insert = new ImFriendShipGroupEntity();
        insert.setCreateTime(System.currentTimeMillis());
        insert.setDelFlag(DelFlagEnum.NORMAL.getCode());
        insert.setAppId(req.getAppId());
        insert.setGroupName(req.getGroupName());
        insert.setSequence(seq);
        insert.setFromId(req.getFromId());
        try {
            int insert1 = imFriendShipGroupMapper.insert(insert);

            if (insert1 != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_CREATE_ERROR);
            }

            if (insert1 == 1 && CollectionUtil.isNotEmpty(req.getToIds())) {
                AddFriendShipGroupMemberReq addFriendShipGroupMemberReq = new AddFriendShipGroupMemberReq();
                addFriendShipGroupMemberReq.setFromId(req.getFromId());
                addFriendShipGroupMemberReq.setGroupName(req.getGroupName());
                addFriendShipGroupMemberReq.setToIds(req.getToIds());
                addFriendShipGroupMemberReq.setAppId(req.getAppId());
                imFriendShipGroupMemberService.addGroupMember(addFriendShipGroupMemberReq);
                writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.FriendshipGroup, seq);
                return ResponseVO.successResponse();
            }
        } catch (
                DuplicateKeyException e) {
            e.getStackTrace();
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
        }
        //TCP通知：发送给除了本端的其他端新建了一个好友分组消息
        AddFriendGroupPack addFriendGropPack = new AddFriendGroupPack();
        addFriendGropPack.setFromId(req.getFromId());
        addFriendGropPack.setSequence(seq);
        addFriendGropPack.setGroupName(req.getGroupName());
        messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_ADD,
                addFriendGropPack,new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));
        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req) {

        for (String groupName : req.getGroupName()) {
            QueryWrapper<ImFriendShipGroupEntity> query = new QueryWrapper<>();
            query.eq("group_name", groupName);
            query.eq("app_id", req.getAppId());
            query.eq("from_id", req.getFromId());
            query.eq("del_flag", DelFlagEnum.NORMAL.getCode());

            ImFriendShipGroupEntity entity = imFriendShipGroupMapper.selectOne(query);

            if (entity != null) {
                long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.FriendshipGroup+":"+req.getFromId());
                ImFriendShipGroupEntity update = new ImFriendShipGroupEntity();
                update.setGroupId(entity.getGroupId());
                update.setDelFlag(DelFlagEnum.DELETE.getCode());
                update.setSequence(seq);
                imFriendShipGroupMapper.updateById(update);
                imFriendShipGroupMemberService.clearGroupMember(entity.getGroupId());
                writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.FriendshipGroup, seq);
                //TCP通知：发送给除了本端的其他端删除了一个好友分组消息
                DeleteFriendGroupPack deleteFriendGroupPack = new DeleteFriendGroupPack();
                deleteFriendGroupPack.setFromId(req.getFromId());
                deleteFriendGroupPack.setGroupName(groupName);
                deleteFriendGroupPack.setSequence(seq);
                messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_DELETE,
                        deleteFriendGroupPack,new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));
            }else{
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_NOT_EXIST);
            }
        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getGroup(String fromId, String groupName, Integer appId) {
        QueryWrapper<ImFriendShipGroupEntity> query = new QueryWrapper<>();
        query.eq("group_name", groupName);
        query.eq("app_id", appId);
        query.eq("from_id", fromId);
        query.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        ImFriendShipGroupEntity entity = imFriendShipGroupMapper.selectOne(query);
        if (entity == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(entity);
    }

}
