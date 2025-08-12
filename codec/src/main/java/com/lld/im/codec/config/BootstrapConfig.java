package com.lld.im.codec.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * ClassName: BootstrapConfig
 * Package: com.lld.im.codec.config
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/28 下午7:24
 * Version 1.0
 */
@Data
public class BootstrapConfig {
    private TcpConfig lim;
    @Data
    public static class TcpConfig {
        private Integer tcpPort;//tcp绑定的端口号
        private Integer webSocketPort;//websocket绑定的端口号
        private boolean enableWebSocket;//是否启用webSocket
        private Integer bossThreadSize;//boss线程 默认=1
        private Integer workThreadSize;//work线程
        private RedisConfig redis;//redis配置
        private Long heartBeatTime; //心跳超时时间 单位毫秒
        private Rabbitmq rabbitmq;//rabbitmq配置
        private ZkConfig zkConfig;//zookeeper配置
        private Integer brokerId;//服务器节点唯一标识
        private Integer loginModel;//多端登录同步模式
        private String logicUrl;

    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisConfig {

        /**
         * 单机模式：single 哨兵模式：sentinel 集群模式：cluster
         */
        private String mode;
        /**
         * 数据库
         */
        private Integer database;
        /**
         * 密码
         */
        private String password;
        /**
         * 超时时间
         */
        private Integer timeout;
        /**
         * 最小空闲数
         */
        private Integer poolMinIdle;
        /**
         * 连接超时时间(毫秒)
         */
        private Integer poolConnTimeout;
        /**
         * 连接池大小
         */
        private Integer poolSize;

        /**
         * redis单机配置
         */
        private RedisSingle single;

    }
    /**
     * redis单机配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisSingle {
        /**
         * 地址
         */
        private String address;
    }
    /**
     * rabbitmq哨兵模式配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rabbitmq {
        private String host;

        private Integer port;

        private String virtualHost;

        private String userName;

        private String password;
    }
    @Data
    public static class ZkConfig {
        /**
         * zk连接地址
         */
        private String zkAddr;

        /**
         * zk连接超时时间
         */
        private Integer zkConnectTimeOut;
    }

}
