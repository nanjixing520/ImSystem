package com.lld.im.tcp;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.receiver.MessageReceiver;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.register.RegistryZK;
import com.lld.im.tcp.register.ZKit;
import com.lld.im.tcp.server.LimServer;
import com.lld.im.tcp.server.LimWebSocketServer;
import com.lld.im.tcp.utils.MqFactory;
import org.I0Itec.zkclient.ZkClient;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ClassName: Starter
 * Package: com.lld.im.tcp
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/28 下午3:47
 * Version 1.0
 */
public class Starter {
    public static void main(String[] args) {
        if (args.length>0) {
            start(args[0]);
        }
    }
    private static void start(String path){
        try{
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);
            new LimServer(bootstrapConfig.getLim()).start();//启动TCP 服务（监听 tcpPort）。
            new LimWebSocketServer(bootstrapConfig.getLim()).start();//启动WebSocket 服务（监听 webSocketPort）
            //项目启动的时候就初始化redis
            RedisManager.init(bootstrapConfig);
            //启动rabbitmq
            MqFactory.init(bootstrapConfig.getLim().getRabbitmq());
            //启动消息的监听
            MessageReceiver.init(bootstrapConfig.getLim().getBrokerId()+"");
            //注册当前服务到 ZooKeeper
            registerZK(bootstrapConfig);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(500);
        }

    }
    public static void registerZK(BootstrapConfig config) throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        //连接ZooKeeper 服务端，拿到一个可以用来读写 ZooKeeper 的客户端
        ZkClient zkClient = new ZkClient(config.getLim().getZkConfig().getZkAddr(),// // ZooKeeper 服务器地址
                config.getLim().getZkConfig().getZkConnectTimeOut());//// 连接超时时间（毫秒）
        ZKit zKit = new ZKit(zkClient);//封装成 ZKit，方便操作 ZooKeeper（创造节点）
        RegistryZK registryZK = new RegistryZK(zKit, hostAddress, config.getLim());
        Thread thread = new Thread(registryZK);//创建一个线程运行 RegistryZK，异步注册服务。
        thread.start();
    }
}
