package com.lld.im.tcp;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

public class RedissonTest {
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.144.147:6379")
                                .setPassword("123321").setDatabase(1);
        //设置解码器，确定了key和value被读取的方式
        StringCodec stringCodec = new StringCodec();
        config.setCodec(stringCodec);
        //根据配置文件新建客户端
        RedissonClient redissonClient = Redisson.create(config);
//         //获取一个名为 "im" 的分布式对象引用，不会立即创建，只有在第一次写入数据时才会真正在 Redis 中创建，”im“是key值
//        RBucket<Object> im = redissonClient.getBucket("im");
//        System.out.println(im.get());
//        im.set("im");
//        System.out.println(im.get());
//
//        RMap<String, String> imMap = redissonClient.getMap("imMap");
//        String client = imMap.get("client");
//        System.out.println(client);
//        imMap.put("client","webClient");
//        System.out.println(imMap.get("client"));

        //redis的发布订阅（发送给所有监听的客户端，不是发送一个，也不是负载均衡的轮询的发送），redis也可以做消息的投递和消费（像rabbitmq一样，但是redis没有办法持久化，服务不可用的时候消息会丢失）
        RTopic topic1 = redissonClient.getTopic("topic");
        topic1.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String s) {
                System.out.println("client1收到消息：" + s);
            }
        });

        RTopic topic2 = redissonClient.getTopic("topic");
        topic2.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String s) {
                System.out.println("client2收到消息：" + s);
            }
        });
        //发布消息
        RTopic topic3 = redissonClient.getTopic("topic");
        topic3.publish("hello");
    }

}