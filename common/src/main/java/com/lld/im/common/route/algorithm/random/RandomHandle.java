package com.lld.im.common.route.algorithm.random;

import com.lld.im.common.enums.UserErrorCode;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ClassName: RandomHandle
 * Package: com.lld.im.common.router.algorithm.Random
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/4 下午4:01
 * Version 1.0
 */
public class RandomHandle implements RouteHandle {
    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if(size == 0){
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        int i = ThreadLocalRandom.current().nextInt(size);//范围[0, size-1] 高效线程安全生成一个索引
        return values.get(i);
    }
}
