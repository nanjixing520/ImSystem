package com.lld.im.common.constant;

/**
 * ClassName: Constants
 * Package: com.lld.im.common.constant
 * Description:
 *   存放系统的常量
 * @Author 南极星
 * @Create 2025/7/30 上午11:38
 * Version 1.0
 */
public class Constants {
    /** channel绑定的userId Key*/
    public static final String UserId = "userId";

    /** channel绑定的appId */
    public static final String AppId = "appId";
    /** channel绑定的客户端类型 */
    public static final String ClientType = "clientType";
    public static final String ReadTime = "readTime";
    public static final String ImCoreZkRoot = "/im-coreRoot";
    public static final String ImCoreZkRootTcp = "/tcp";
    public static final String ImCoreZkRootWeb = "/web";
    public static final String Imei = "imei";


    public static class RedisConstants{
        /**
         * 用户session: appId + UserSessionConstants + 用户id  例如：10000：userSession:lld
         */
        public static final String UserSessionConstants = ":userSession:";
        /**
         * 用户上线通知channel
         */
        public static final String UserLoginChannel
                = "signal/channel/LOGIN_USER_INNER_QUEUE";
        /**
         * userSign，格式：appId:userSign:
         */
        public static final String userSign = "userSign";
        /**
         * 缓存客户端消息防重，格式： appId + :cacheMessage: + messageId
         */
        public static final String cacheMessage = "cacheMessage";
        /**
         * 离线消息
         */
        public static final String OfflineMessage = "offlineMessage";
        /**
         * seq 前缀
         */
        public static final String SeqPrefix = "seq";

        /**
         * 用户订阅列表，格式 ：appId + :subscribe: + userId。Hash结构，filed为订阅自己的人
         */
        public static final String subscribe = "subscribe";

        /**
         * 用户自定义在线状态，格式 ：appId + :userCustomerStatus: + userId。set，value为用户id
         */
        public static final String userCustomerStatus = "userCustomerStatus";

    }
    public static class RabbitConstants{

        public static final String Im2UserService = "pipeline2UserService";

        public static final String Im2MessageService = "pipeline2MessageService";

        public static final String Im2GroupService = "pipeline2GroupService";

        public static final String Im2FriendshipService = "pipeline2FriendshipService";

        public static final String MessageService2Im = "messageService2Pipeline";

        public static final String GroupService2Im = "GroupService2Pipeline";

        public static final String FriendShip2Im = "friendShip2Pipeline";

        public static final String StoreP2PMessage = "storeP2PMessage";

        public static final String StoreGroupMessage = "storeGroupMessage";
    }
    public static class CallbackCommand{
        public static final String ModifyUserAfter = "user.modify.after";

        public static final String CreateGroupAfter = "group.create.after";

        public static final String UpdateGroupAfter = "group.update.after";

        public static final String DestoryGroupAfter = "group.destory.after";

        public static final String TransferGroupAfter = "group.transfer.after";

        public static final String GroupMemberAddBefore = "group.member.add.before";

        public static final String GroupMemberAddAfter = "group.member.add.after";

        public static final String GroupMemberDeleteAfter = "group.member.delete.after";

        public static final String AddFriendBefore = "friend.add.before";

        public static final String AddFriendAfter = "friend.add.after";

        public static final String UpdateFriendBefore = "friend.update.before";

        public static final String UpdateFriendAfter = "friend.update.after";

        public static final String DeleteFriendAfter = "friend.delete.after";

        public static final String AddBlackAfter = "black.add.after";

        public static final String DeleteBlack = "black.delete";

        public static final String SendMessageAfter = "message.send.after";

        public static final String SendMessageBefore = "message.send.before";

    }
    public static class SeqConstants {
        public static final String Message = "messageSeq";

        public static final String GroupMessage = "groupMessageSeq";


        public static final String Friendship = "friendshipSeq";

//        public static final String FriendshipBlack = "friendshipBlackSeq";

        public static final String FriendshipRequest = "friendshipRequestSeq";

        public static final String FriendshipGroup = "friendshipGrouptSeq";

        public static final String Group = "groupSeq";

        public static final String Conversation = "conversationSeq";

    }


}
