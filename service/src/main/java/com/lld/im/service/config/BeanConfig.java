package com.lld.im.service.config;

import com.lld.im.common.config.AppConfig;
import com.lld.im.common.enums.ImUrlRouteWayEnum;
import com.lld.im.common.enums.RouteHashMethodEnum;
import com.lld.im.common.route.RouteHandle;
import com.lld.im.common.route.algorithm.consistenthash.AbstractConsistentHash;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * ClassName: BeanConfig
 * Package: com.lld.im.service.config
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/4 下午4:11
 * Version 1.0
 */
@Configuration
public class BeanConfig {
    @Autowired
    private AppConfig appConfig;
//    @Bean
//    public RouteHandle routeHandle() {
//        //return new RandomHandle();
//        //return new LoopHandle();
//        ConsistentHashHandle consistentHashHandle = new ConsistentHashHandle();
//        TreeMapConsistentHash treeMapConsistentHash = new TreeMapConsistentHash();
//        consistentHashHandle.setHash(treeMapConsistentHash);
//        return consistentHashHandle;
//    }
@Bean
public RouteHandle routeHandle() throws Exception {

    Integer imRouteWay = appConfig.getImRouteWay();
    String routWay = "";

    ImUrlRouteWayEnum handler = ImUrlRouteWayEnum.getHandler(imRouteWay);
    routWay = handler.getClazz();

    RouteHandle routeHandle = (RouteHandle) Class.forName(routWay).newInstance();
    if(handler == ImUrlRouteWayEnum.HASH){

        Method setHash = Class.forName(routWay).getMethod("setHash", AbstractConsistentHash.class);
        Integer consistentHashWay = appConfig.getConsistentHashWay();
        String hashWay = "";

        RouteHashMethodEnum hashHandler = RouteHashMethodEnum.getHandler(consistentHashWay);
        hashWay = hashHandler.getClazz();
        AbstractConsistentHash consistentHash
                = (AbstractConsistentHash) Class.forName(hashWay).newInstance();
        setHash.invoke(routeHandle,consistentHash);
    }

    return routeHandle;
}

    @Bean
    public ZkClient zkClient() {
        return new ZkClient(appConfig.getZkAddr(), appConfig.getZkConnectTimeOut());
    }
}
