package com.lld.im.common.route.algorithm.consistenthash;

import com.lld.im.common.route.RouteHandle;

import java.util.List;

/**
 * ClassName: ConsistentHashHandle
 * Package: com.lld.im.common.router.algorithm.consistenthash
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/4 下午8:59
 * Version 1.0
 */
public class ConsistentHashHandle implements RouteHandle {

    //TreeMap
    private AbstractConsistentHash hash;

    public void setHash(AbstractConsistentHash hash) {
        this.hash = hash;
    }

    @Override
    public String routeServer(List<String> values, String key) {
        return hash.process(values,key);
    }
}
