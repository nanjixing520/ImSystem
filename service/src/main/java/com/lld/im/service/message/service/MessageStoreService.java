package com.lld.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.model.message.*;
import com.lld.im.service.conversation.service.ConversationService;
import com.lld.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.lld.im.service.message.dao.ImMessageBodyEntity;
import com.lld.im.service.message.dao.ImMessageHistoryEntity;
import com.lld.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.lld.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.lld.im.service.utils.SnowflakeIdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: MessageStoreService
 * Package: com.lld.im.service.message.service
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/15 上午9:08
 * Version 1.0
 */
@Service
public class MessageStoreService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    AppConfig appConfig;
    @Autowired
    ConversationService conversationService;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent){
//        //客户端传来的messageContent 转化成 messageBody消息体
//        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
//        //插入messageBody到数据库中
//        imMessageBodyMapper.insert(imMessageBodyEntity);
//        //提取出两条MessageHistory消息索引记录实体类
//        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(messageContent, imMessageBodyEntity);
//        //批量插入(私聊用写扩散，给双方各插入一条消息记录，主要是消息的拥有者不同)
//        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
//        //给客户端返回的消息中设置用雪花算法生成的消息主键
//        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
        DoStoreP2PMessageDto dto = new DoStoreP2PMessageDto();
        ImMessageBody imMessageBody = extractMessageBody(messageContent);
        dto.setMessageBody(imMessageBody);
        dto.setMessageContent(messageContent);
        messageContent.setMessageKey(imMessageBody.getMessageKey());
        //将消息投递到mq中去
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreP2PMessage,"", JSONObject.toJSONString(dto));
    }

    public ImMessageBody extractMessageBody(MessageContent messageContent){
        ImMessageBody messageBody = new ImMessageBody();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(snowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        messageBody.setMessageBody(messageContent.getMessageBody());
        return messageBody;
    }

    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent,
                                                                   ImMessageBodyEntity imMessageBodyEntity){
        List<ImMessageHistoryEntity> list = new ArrayList<>();

        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }
    @Transactional
    public void storeGroupMessage(GroupChatMessageContent messageContent){
        //优化：通过mq异步持久化数据
        ImMessageBody imMessageBody = extractMessageBody(messageContent);
        DoStoreGroupMessageDto dto = new DoStoreGroupMessageDto();
        dto.setGroupChatMessageContent(messageContent);
        dto.setMessageBody(imMessageBody);
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreGroupMessage,
                "",
                JSONObject.toJSONString(dto));
        messageContent.setMessageKey(imMessageBody.getMessageKey());
//        //客户端传来的messageContent 转化成 messageBody消息体
//        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
//        //插入messageBody到数据库中
//        imMessageBodyMapper.insert(imMessageBodyEntity);
//        //转换成GroupMessageHistory群聊消息索引记录实体类
//        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, imMessageBodyEntity);
//        //单条插入（群聊用读扩散，一条群聊消息对话信息只存储一次）
//        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
//        //给客户端返回的消息中设置用雪花算法生成的消息主键
//        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent
                                                                             messageContent , ImMessageBodyEntity messageBodyEntity){
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }

    /**
     * 将从缓存中获取消息内容改的更加通用了，
     * 传入一个指定类型对象
     * 根据指定返回对应的类（私聊和群聊均可以使用）
     * <T> 是 “声明一个泛型类型变量 T”，T（返回值）是 “方法返回 T 类型的对象”。
     *    两者结合，让方法可以灵活处理任意类型的转换与返回，实现 “一次编写，多类型复用”。
     * @param appId
     * @param messageId
     * @return
     */
    public <T> T getMessageFromMessageIdCache(Integer appId, String messageId,Class<T> clazz) {
        //appid : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        String msg = stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isBlank(msg)){
            return null;
        }
        return JSONObject.parseObject(msg,clazz);
    }

    /**
     * 将消息存到缓存中改的更加通用
     * 传入的消息类型可以是私聊消息也可以是群聊消息（私聊和群聊均可以使用）
     * @param appId
     * @param messageId
     * @param messageContent
     */
    public void setMessageFromMessageIdCache(Integer appId,String messageId,Object messageContent){
        //appid : cache : messageId
        String key =appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key,JSONObject.toJSONString(messageContent),300, TimeUnit.SECONDS);
    }

    /**
     * 存储私聊离线消息
     * @param offlineMessage
     */
    public void storeOfflineMessage(OfflineMessageContent offlineMessage){

        // 找到fromId的队列
        String fromKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getFromId();
        // 找到toId的队列
        String toKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getToId();

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        //判断 队列中的数据数量是否超过设定值
        if(operations.zCard(fromKey) >= appConfig.getOfflineMessageCount()){
            //删除 ZSet 中 分值最小的那条消息（即最早收到的离线消息）
            operations.removeRange(fromKey,0,0);//包含首尾索引的区间删除
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(),offlineMessage.getFromId(),offlineMessage.getToId()
        ));
        // 插入数据 根据messageKey 作为分值
        operations.add(fromKey,JSONObject.toJSONString(offlineMessage),
                offlineMessage.getMessageKey());

        //判断 队列中的数据数量是否超过设定值
        if(operations.zCard(toKey) >= appConfig.getOfflineMessageCount()){
            operations.removeRange(toKey,0,0);
        }

        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(),offlineMessage.getToId(),offlineMessage.getFromId()
        ));
        // 插入 数据 根据messageKey 作为分值
        operations.add(toKey,JSONObject.toJSONString(offlineMessage),
                offlineMessage.getMessageKey());

    }

    /**
     * 存储群聊离线消息
     * @param offlineMessage
     * @param memberIds
     */
    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage
            ,List<String> memberIds){

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        offlineMessage.setConversationType(ConversationTypeEnum.GROUP.getCode());

        for (String memberId : memberIds) {
            // 找到toId的队列
            String toKey = offlineMessage.getAppId() + ":" +
                    Constants.RedisConstants.OfflineMessage + ":" +
                    memberId;
            offlineMessage.setConversationId(conversationService.convertConversationId(
                    ConversationTypeEnum.GROUP.getCode(),memberId,offlineMessage.getToId()
            ));
            //判断 队列中的数据数量是否超过设定值
            if(operations.zCard(toKey) >= appConfig.getOfflineMessageCount()){
                operations.removeRange(toKey,0,0);
            }
            // 插入 数据 根据messageKey 作为分值
            operations.add(toKey,JSONObject.toJSONString(offlineMessage),
                    offlineMessage.getMessageKey());
        }


    }

}

