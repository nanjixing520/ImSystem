package com.lld.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.codec.pack.conversation.DeleteConversationPack;
import com.lld.im.codec.pack.conversation.UpdateConversationPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.enums.ConversationErrorCode;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.command.ConversationEventCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.message.MessageReadedContent;
import com.lld.im.service.conversation.dao.ImConversationSetEntity;
import com.lld.im.service.conversation.dao.mapper.ImConversationSetMapper;
import com.lld.im.service.conversation.model.DeleteConversationReq;
import com.lld.im.service.conversation.model.UpdateConversationReq;
import com.lld.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ClassName: ConversationService
 * Package: com.lld.im.service.conversation.service
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/20 上午10:43
 * Version 1.0
 */
@Service
public class ConversationService {
    @Autowired
    ImConversationSetMapper imConversationSetMapper;
    @Autowired
    AppConfig appConfig;
    @Autowired
    MessageProducer messageProducer;

    public String convertConversationId(Integer type,String fromId,String toId){
        return type + "_" + fromId + "_" + toId;
    }

    public void  messageMarkRead(MessageReadedContent messageReadedContent){
        String toId = messageReadedContent.getToId();
        if(messageReadedContent.getConversationType() == ConversationTypeEnum.GROUP.getCode()){
            toId = messageReadedContent.getGroupId();
        }
        String conversationId = convertConversationId(messageReadedContent.getConversationType(),
                messageReadedContent.getFromId(), toId);
        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("conversation_id",conversationId);
        query.eq("app_id",messageReadedContent.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(query);
        if(imConversationSetEntity == null){
            imConversationSetEntity = new ImConversationSetEntity();
//            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent,imConversationSetEntity);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetEntity.setToId(toId);
//            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.insert(imConversationSetEntity);
//            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(),
//                    messageReadedContent.getFromId(),Constants.SeqConstants.Conversation,seq);
        }else{
//            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
//            imConversationSetEntity.setSequence(seq);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetMapper.readMark(imConversationSetEntity);
//            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(),
//                    messageReadedContent.getFromId(),Constants.SeqConstants.Conversation,seq);
        }
    }

    /**
     * 删除会话
     * 1.根据业务需求选择是否需要重置置顶，免打扰等属性（需要就更新表中信息）
     * 2.如果删除会话多端同步开启，那么就通知其他各端也进行删除会话操作
     * @param req
     * @return
     */
    public ResponseVO deleteConversation(DeleteConversationReq req){

        //根据业务需求选择是否需要重置置顶，免打扰等属性
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id",req.getConversationId());
        queryWrapper.eq("app_id",req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if(imConversationSetEntity != null){
            imConversationSetEntity.setIsMute(0);
            imConversationSetEntity.setIsTop(0);
            imConversationSetMapper.update(imConversationSetEntity,queryWrapper);
        }else{
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_IS_NOT_EXIST);
        }
        //如果删除会话多端同步开启，那么就通知其他各端也进行删除会话操作
        if(appConfig.getDeleteConversationSyncMode() == 1){
            DeleteConversationPack pack = new DeleteConversationPack();
            pack.setConversationId(req.getConversationId());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_DELETE,
                    pack,new ClientInfo(req.getAppId(),req.getClientType(),
                            req.getImei()));
        }
        return ResponseVO.successResponse();
    }

    /**
     * 更新会话 置顶or免打扰
     * 更新会话表中信息之后还需要同步给其他端
     * @param req
     * @return
     */
    public ResponseVO updateConversation(UpdateConversationReq req){
        if(req.getIsTop() == null && req.getIsMute() == null){
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id",req.getConversationId());
        queryWrapper.eq("app_id",req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if(imConversationSetEntity != null){
//            long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Conversation);

            if(req.getIsMute() != null){
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            if(req.getIsMute() != null){
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
//            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.update(imConversationSetEntity,queryWrapper);
//            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(),
//                    Constants.SeqConstants.Conversation, seq);

            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setIsMute(imConversationSetEntity.getIsMute());
            pack.setIsTop(imConversationSetEntity.getIsTop());
//            pack.setSequence(seq);
            pack.setConversationType(imConversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_UPDATE,
                    pack,new ClientInfo(req.getAppId(),req.getClientType(),
                            req.getImei()));
        }else{
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse();
    }

}
