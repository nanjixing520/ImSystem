package com.lld.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.codec.pack.friendship.ApproverFriendRequestPack;
import com.lld.im.codec.pack.friendship.ReadAllFriendRequestPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ApproverFriendRequestStatusEnum;
import com.lld.im.common.enums.FriendShipErrorCode;
import com.lld.im.common.enums.command.FriendshipEventCommand;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.service.friendship.dao.ImFriendShipRequestEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipRequestMapper;
import com.lld.im.service.friendship.model.req.ApproverFriendRequestReq;
import com.lld.im.service.friendship.model.req.FriendDto;
import com.lld.im.service.friendship.model.req.ReadFriendShipRequestReq;
import com.lld.im.service.friendship.service.ImFriendShipService;
import com.lld.im.service.friendship.service.ImFriendShipRequestService;
import com.lld.im.service.seq.RedisSeq;
import com.lld.im.service.utils.MessageProducer;
import com.lld.im.service.utils.WriteUserSeq;
import org.apache.commons.lang3.StringUtils;
import org.redisson.client.RedisAuthRequiredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * ClassName: ImFriendShipRequestServiceImpl
 * Package: com.lld.im.service.friendship.service.impl
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/21 上午9:47
 * Version 1.0
 */
@Service
public class ImFriendShipRequestServiceImpl implements ImFriendShipRequestService {

    @Autowired
    private ImFriendShipRequestMapper imFriendShipRequestMapper;
    @Autowired
    private ImFriendShipService imFriendService;
    @Autowired
    private MessageProducer messageProducer;
    @Autowired
    RedisSeq redisSeq;
    @Autowired
    WriteUserSeq writeUserSeq;

    //A + B
    @Override
    public ResponseVO addFienshipRequest(String fromId, FriendDto dto, Integer appId) {

        QueryWrapper<ImFriendShipRequestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id",appId);
        queryWrapper.eq("from_id",fromId);
        queryWrapper.eq("to_id",dto.getToId());
        ImFriendShipRequestEntity request = imFriendShipRequestMapper.selectOne(queryWrapper);
        long seq = redisSeq.doGetSeq(appId+":"+
                Constants.SeqConstants.FriendshipRequest+":"+dto.getToId());
        //好友申请数据中没有，说明之前从未加过好友，将此条关系入库，等待审批
        if(request == null){
            request = new ImFriendShipRequestEntity();
            request.setAddSource(dto.getAddSource());
            request.setAddWording(dto.getAddWording());
            request.setAppId(appId);
            request.setSequence(seq);
            request.setFromId(fromId);
            request.setToId(dto.getToId());
            request.setReadStatus(0);
            request.setApproveStatus(0);
            request.setRemark(dto.getRemark());
            request.setCreateTime(System.currentTimeMillis());
            imFriendShipRequestMapper.insert(request);

        }else {//两人关系可能是互删或者是之前就未审批通过（但是之前有过申请记录，更新申请信息）
            //修改记录内容 和更新时间
            if(StringUtils.isNotBlank(dto.getAddSource())){
                request.setAddWording(dto.getAddWording());
            }
            if(StringUtils.isNotBlank(dto.getRemark())){
                request.setRemark(dto.getRemark());
            }
            if(StringUtils.isNotBlank(dto.getAddWording())){
                request.setAddWording(dto.getAddWording());
            }
            request.setApproveStatus(0);
            request.setReadStatus(0);
            request.setSequence(seq);
            imFriendShipRequestMapper.updateById(request);
        }
        //写到对方的好友申请序列号维护hash中，这样对方增量获取的时候就能看到咱的好友申请了
        writeUserSeq.writeUserSeq(appId,dto.getToId(),
                Constants.SeqConstants.FriendshipRequest,seq);
        //TCP通知：发送好友申请的通知给接收方的所有端
        messageProducer.sendToUser(dto.getToId(),
                FriendshipEventCommand.FRIEND_REQUEST,
                request, appId);
        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO approverFriendRequest(ApproverFriendRequestReq req) {

        ImFriendShipRequestEntity imFriendShipRequestEntity = imFriendShipRequestMapper.selectById(req.getId());
        if(imFriendShipRequestEntity == null){
            throw new ApplicationException(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);
        }

        if(!req.getOperater().equals(imFriendShipRequestEntity.getToId())){
            //只能审批发给自己的好友请求
            throw new ApplicationException(FriendShipErrorCode.NOT_APPROVER_OTHER_MAN_REQUEST);
        }
        long seq = redisSeq.doGetSeq(req.getAppId()+":"+
                Constants.SeqConstants.FriendshipRequest+":"+req.getOperater());

        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setApproveStatus(req.getStatus());
        update.setUpdateTime(System.currentTimeMillis());
        update.setId(req.getId());
        update.setSequence(seq);
        imFriendShipRequestMapper.updateById(update);
        writeUserSeq.writeUserSeq(req.getAppId(),req.getOperater(),
                Constants.SeqConstants.FriendshipRequest,seq);

        if(ApproverFriendRequestStatusEnum.AGREE.getCode() == req.getStatus()){
            //同意 ===> 去执行添加好友逻辑
            FriendDto dto = new FriendDto();
            dto.setAddSource(imFriendShipRequestEntity.getAddSource());
            dto.setAddWording(imFriendShipRequestEntity.getAddWording());
            dto.setRemark(imFriendShipRequestEntity.getRemark());
            dto.setToId(imFriendShipRequestEntity.getToId());
            ResponseVO responseVO = imFriendService.doAddFriend(req,imFriendShipRequestEntity.getFromId(), dto,req.getAppId());
//            if(!responseVO.isOk()){
////                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//                return responseVO;
//            }
            if(!responseVO.isOk() && responseVO.getCode() != FriendShipErrorCode.TO_IS_YOUR_FRIEND.getCode()){
                return responseVO;
            }
        }
        //TCP通知：通知除了自己审批结果所在的客户端其他所有的端自己的审批结果
        ApproverFriendRequestPack approverFriendRequestPack = new ApproverFriendRequestPack();
        approverFriendRequestPack.setId(req.getId());
        approverFriendRequestPack.setStatus(req.getStatus());
        approverFriendRequestPack.setSequence(seq);
        messageProducer.sendToUser(imFriendShipRequestEntity.getToId(),req.getClientType(),req.getImei(), FriendshipEventCommand
                .FRIEND_REQUEST_APPROVER,approverFriendRequestPack,req.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req) {
        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("to_id", req.getFromId());
        long seq = redisSeq.doGetSeq(req.getAppId()+":"+
                Constants.SeqConstants.FriendshipRequest+":"+req.getOperater());
        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setReadStatus(1);
        update.setSequence(seq);
        imFriendShipRequestMapper.update(update, query);
        writeUserSeq.writeUserSeq(req.getAppId(),req.getOperater(),
                Constants.SeqConstants.FriendshipRequest,seq);
        //TCP通知：通知除了自己已读好友请求所在的客户端其他所有的端将请求消息已读了
        ReadAllFriendRequestPack readAllFriendRequestPack = new ReadAllFriendRequestPack();
        readAllFriendRequestPack.setFromId(req.getFromId());
        readAllFriendRequestPack.setSequence(seq);
        messageProducer.sendToUser(req.getFromId(),req.getClientType(),req.getImei(),FriendshipEventCommand
                .FRIEND_REQUEST_READ,readAllFriendRequestPack,req.getAppId());
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getFriendRequest(String fromId, Integer appId) {
        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper();
        query.eq("app_id", appId);
        query.eq("to_id", fromId);

        List<ImFriendShipRequestEntity> requestList = imFriendShipRequestMapper.selectList(query);

        return ResponseVO.successResponse(requestList);
    }
}

