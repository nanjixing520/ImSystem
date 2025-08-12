package com.lld.im.common.utils;

import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.route.RouteInfo;


/**
 * ClassName: RouteInfoParseUtil
 * Package: com.lld.im.common.utils
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/4 下午4:41
 * Version 1.0
 */
public class RouteInfoParseUtil {
    public static RouteInfo parse(String info){
        try {
            String[] serverInfo = info.split(":");
            RouteInfo routeInfo =  new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1])) ;
            return routeInfo;
        }catch (Exception e){
            throw new ApplicationException(BaseErrorCode.PARAMETER_ERROR) ;
        }
    }
}
