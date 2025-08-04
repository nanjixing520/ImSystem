package com.lld.im.tcp.register;

import com.lld.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;

/**
 * ClassName: ZKit
 * Package: com.lld.im.tcp.register
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/31 下午7:48
 * Version 1.0
 */
public class ZKit {
    private ZkClient zkClient;

    public ZKit(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    //im-coreRoot/tcp/ip:port
    public void createRootNode(){
        boolean exists = zkClient.exists(Constants.ImCoreZkRoot);
        if(!exists){
            zkClient.createPersistent(Constants.ImCoreZkRoot);
        }
        boolean tcpExists = zkClient.exists(Constants.ImCoreZkRoot +
                Constants.ImCoreZkRootTcp);
        if(!tcpExists){
            zkClient.createPersistent(Constants.ImCoreZkRoot +
                    Constants.ImCoreZkRootTcp);
        }

        boolean webExists = zkClient.exists(Constants.ImCoreZkRoot +
                Constants.ImCoreZkRootWeb);
        if(!webExists){
            zkClient.createPersistent(Constants.ImCoreZkRoot +
                    Constants.ImCoreZkRootWeb);
        }
    }

    //ip+port
    public void createNode(String path){
        if(!zkClient.exists(path)){
            //创建一个持久节点（Persistent ZNode），节点会一直存在，不会因为客户端断开而自动删除。
            zkClient.createPersistent(path);
        }
    }
}
