package com.lld.im.service.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.pack.user.UserCustomStatusChangeNotifyPack;
import com.lld.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.UserSession;
import com.lld.im.service.friendship.service.ImFriendShipService;
import com.lld.im.service.user.model.UserStatusChangeNotifyContent;
import com.lld.im.service.user.model.req.PullFriendOnlineStatusReq;
import com.lld.im.service.user.model.req.PullUserOnlineStatusReq;
import com.lld.im.service.user.model.req.SetUserCustomerStatusReq;
import com.lld.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.lld.im.service.user.model.resp.UserOnlineStatusResp;
import com.lld.im.service.user.service.ImUserStatusService;
import com.lld.im.service.utils.MessageProducer;
import com.lld.im.service.utils.UserSessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: ImUserStatusServiceImpl
 * Package: com.lld.im.service.user.service.impl
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/22 下午8:53
 * Version 1.0
 */
@Service
public class ImUserStatusServiceImpl implements ImUserStatusService {
    @Autowired
    UserSessionUtils userSessionUtils;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImFriendShipService imFriendService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content) {
       //获取该用户在线的所有客户端信息(可能一端下线但是该用户还有其他端在线，所以将所有在线信息都投递，具体怎么处理展示交给客户端)
        List<UserSession> userSession = userSessionUtils.getUserSession(content.getAppId(), content.getUserId());
        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        BeanUtils.copyProperties(content,userStatusChangeNotifyPack);
        userStatusChangeNotifyPack.setClient(userSession);
        //同步给其他端
        syncSender(userStatusChangeNotifyPack,content.getUserId(),
                content);
        //通知好友和订阅我状态的人
        dispatcher(userStatusChangeNotifyPack,content.getUserId(),
                content.getAppId());
    }

    @Override
    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req) {
        Long subExpireTime = 0L;
        if(req.getSubTime() != null && req.getSubTime() > 0){
            //设置过期时间
            subExpireTime = System.currentTimeMillis() + req.getSubTime();
        }
        //为每一个被订阅的用户增加订阅者，在redis中维护起来
        //当被订阅的用户在线状态发生变化 就可以遍历发送给订阅者
        //被订阅用户：订阅者1 过期时间1
        //         订阅者2 过期时间2
        for (String beSubUserId : req.getSubUserId()) {
            String userKey = req.getAppId() + ":" + Constants.RedisConstants.subscribe + ":" + beSubUserId;
            stringRedisTemplate.opsForHash().put(userKey,req.getOperater(),subExpireTime.toString());
        }
    }

    /**
     * 用户自行设置其客户端状态
     * 1.将自定义状态记录下来
     * 2.并且通知给其他人（包含同步端，好友和订阅者）
     * @param req
     */
    @Override
    public void setUserCustomerStatus(SetUserCustomerStatusReq req) {
        UserCustomStatusChangeNotifyPack userCustomStatusChangeNotifyPack = new UserCustomStatusChangeNotifyPack();
        userCustomStatusChangeNotifyPack.setCustomStatus(req.getCustomStatus());
        userCustomStatusChangeNotifyPack.setCustomText(req.getCustomText());
        userCustomStatusChangeNotifyPack.setUserId(req.getUserId());
        stringRedisTemplate.opsForValue().set(req.getAppId()
                        +":"+ Constants.RedisConstants.userCustomerStatus + ":" + req.getUserId()
                , JSONObject.toJSONString(userCustomStatusChangeNotifyPack));

        syncSender(userCustomStatusChangeNotifyPack,
                req.getUserId(),new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));
        dispatcher(userCustomStatusChangeNotifyPack,req.getUserId(),req.getAppId());
    }


    private void syncSender(Object pack, String userId, ClientInfo clientInfo){
        messageProducer.sendToUserExceptClient(userId,
                UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC,
                pack,clientInfo);
    }

    private void dispatcher(Object pack,String userId,Integer appId){
        //通知所有盆友
        List<String> allFriendId = imFriendService.getAllFriendId(userId, appId);
        for (String fid : allFriendId) {
            messageProducer.sendToUser(fid,UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                    pack,appId);
        }
        //通知所有订阅我状态变化的订阅者
        String userKey = appId + ":" + Constants.RedisConstants.subscribe +":"+ userId;
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
        for (Object key : keys) {
            //获取订阅者的id
            String filed = (String) key;
            Long expire = Long.valueOf((String) stringRedisTemplate.opsForHash().get(userKey, filed));
            //如果该订阅者订阅时间还没有过期，就发送该用户的状态变化给该订阅者
            //否则就删除该过期的订阅信息
            if(expire > 0 && expire > System.currentTimeMillis()){
                messageProducer.sendToUser(filed,UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                        pack,appId);
            }else{
                stringRedisTemplate.opsForHash().delete(userKey,filed);
            }
        }
    }
    @Override
    public Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req) {
        //获取好友列表
        List<String> allFriendId = imFriendService.getAllFriendId(req.getOperater(), req.getAppId());
        return getUserOnlineStatus(allFriendId,req.getAppId());
    }

    @Override
    public Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req) {
        return getUserOnlineStatus(req.getUserList(),req.getAppId());
    }

    /**
     * 获得用户列表中各用户的在线状态
     * @param userId 传入指定的用户列表
     * @param appId
     * @return
     */
    private Map<String, UserOnlineStatusResp> getUserOnlineStatus(List<String> userId,Integer appId){

        Map<String, UserOnlineStatusResp> result = new HashMap<>(userId.size());
        for (String uid : userId) {

            UserOnlineStatusResp resp = new UserOnlineStatusResp();
            //获取并设置该用户在服务端的真实在线状态
            List<UserSession> userSession = userSessionUtils.getUserSession(appId, uid);
            resp.setSession(userSession);
            //获取并设置该用户自定义的客户端在线状态
            String userKey = appId + ":" + Constants.RedisConstants.userCustomerStatus + ":" + uid;
            String s = stringRedisTemplate.opsForValue().get(userKey);
            if(StringUtils.isNotBlank(s)){
                JSONObject parse = (JSONObject) JSON.parse(s);
                resp.setCustomText(parse.getString("customText"));
                resp.setCustomStatus(parse.getInteger("customStatus"));
            }
            result.put(uid,resp);
        }
        return result;
    }
}
