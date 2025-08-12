package com.lld.im.common.route.algorithm.consistenthash;

import com.lld.im.common.enums.UserErrorCode;
import com.lld.im.common.exception.ApplicationException;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * ClassName: TreeMapConsistentHash
 * Package: com.lld.im.common.router.algorithm.consistenthash
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/4 下午9:00
 * Version 1.0
 */
public class TreeMapConsistentHash extends AbstractConsistentHash {

    // 使用TreeMap存储哈希环
    private TreeMap<Long, String> treeMap = new TreeMap<>();

    // 每个服务节点生成的虚拟节点数
    private static final int NODE_SIZE = 2;

    // 重写添加节点的方法
    @Override
    protected void add(long key, String value) {
        for (int i = 0; i < NODE_SIZE; i++) {
            // 生成虚拟节点的哈希值（如：node123456_0）
            treeMap.put(super.hash("node" + key + i), value);
        }
        treeMap.put(key, value); // 添加原始节点
    }

    // 重写获取节点的方法
    @Override
    protected String getFirstNodeValue(String value) {
        Long hash = super.hash(value); // 计算请求的哈希值
        SortedMap<Long, String> tailMap = treeMap.tailMap(hash); // 找到哈希环中>=hash的部分
        if (!tailMap.isEmpty()) {
            return tailMap.get(tailMap.firstKey()); // 返回第一个匹配的节点
        }
        // 如果哈希环为空，抛出异常
        if (treeMap.size() == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        return treeMap.firstEntry().getValue(); // 返回哈希环的第一个节点
    }

    // 重写初始化方法
    @Override
    protected void processBefore() {
        treeMap.clear(); // 清空哈希环，防止上次服务存储的节点还存在环中
    }
}
