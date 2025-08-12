package com.lld.im.common.route.algorithm.consistenthash;

/**
 * ClassName: AbstractConsistentHash
 * Package: com.lld.im.common.router.algorithm.consistenthash
 * Description:
 *  一致性hash抽象类
 * @Author 南极星
 * @Create 2025/8/4 下午9:05
 * Version 1.0
 */

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @description: 一致性hash 抽象类
 * @author: 南极星
 * @version: 1.0
 */
public abstract class AbstractConsistentHash {

    // 抽象方法：添加节点
    protected abstract void add(long key, String value);

    // 排序方法（空实现，由子类决定是否需要）
    protected void sort(){}

    // 抽象方法：获取第一个匹配的节点
    protected abstract String getFirstNodeValue(String value);

    /**
     * 处理之前的初始化操作
     */
    protected abstract void processBefore();

    /**
     * 传入节点列表以及客户端信息获取一个服务节点
     * @param values 服务节点列表
     * @param key 客户端标识（用于计算哈希）
     * @return 选中的服务节点
     */
    public synchronized String process(List<String> values, String key){
        processBefore(); // 初始化操作
        for (String value : values) {
            add(hash(value), value); // 将每个服务节点加入哈希环
        }
        sort(); // 排序（空实现，可由子类重写）
        return getFirstNodeValue(key); // 返回匹配的服务节点
    }

    // 哈希计算方法
    public Long hash(String value){
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5"); // 获取MD5实例
        } catch (
                NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
        md5.reset(); // 重置MD5实例
        byte[] keyBytes = null;
        try {
            keyBytes = value.getBytes("UTF-8"); // 将字符串转换为UTF-8字节数组
        } catch (
                UnsupportedEncodingException e) {
            throw new RuntimeException("Unknown string :" + value, e);
        }

        md5.update(keyBytes); // 更新MD5摘要
        byte[] digest = md5.digest(); // 计算哈希值

        // 将MD5摘要转换为32位长整型
        long hashCode = ((long) (digest[3] & 0xFF) << 24)
                | ((long) (digest[2] & 0xFF) << 16)
                | ((long) (digest[1] & 0xFF) << 8)
                | (digest[0] & 0xFF);

        long truncateHashCode = hashCode & 0xffffffffL; // 截断为32位
        return truncateHashCode;
    }
}