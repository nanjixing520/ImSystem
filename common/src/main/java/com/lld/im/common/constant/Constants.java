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
}
