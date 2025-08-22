package com.lld.im.service.user.controller;

import com.lld.im.common.ClientType;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.route.RouteHandle;
import com.lld.im.common.route.RouteInfo;
import com.lld.im.common.utils.RouteInfoParseUtil;
import com.lld.im.service.user.model.req.DeleteUserReq;
import com.lld.im.service.user.model.req.GetUserSequenceReq;
import com.lld.im.service.user.model.req.ImportUserReq;
import com.lld.im.service.user.model.req.LoginReq;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.ZKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: ImUserController
 * Package: com.lld.im.service.user.controller
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/17 下午5:31
 * Version 1.0
 */
@RestController
@RequestMapping("v1/user")
public class ImUserController {
    @Autowired
    private ImUserService imUserService;
    @Autowired
    private RouteHandle routeHandle;
    @Autowired
    private ZKit zKit;
    @RequestMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req,
                                 Integer appId){
        req.setAppId(appId);
        return imUserService.importUser(req);
    }
    @RequestMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }
    /**
     * @param req
     * @return com.lld.im.common.ResponseVO
     * @description im的登录接口，返回im地址
     * @author chackylee
     */
    @RequestMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req, Integer appId) {
        req.setAppId(appId);
        ResponseVO login = imUserService.login(req);
        if (login.isOk()) {
            //TODO:去zk获取一个im的地址，返回给sdk（分解为先从zk中获取若干im地址，再通过策略获取一个返回）
            List<String> allNode = new ArrayList<>();
            if (req.getClientType() == ClientType.WEB.getCode()) {
                allNode = zKit.getAllWebNode();
            } else {
                allNode = zKit.getAllTcpNode();
            }
            String s = routeHandle.routeServer(allNode, req
                    .getUserId());
            RouteInfo parse = RouteInfoParseUtil.parse(s);//将一个完整的地址解析成包含两个成员变量（ip和port）的对象
            return ResponseVO.successResponse(parse);
        }

        return ResponseVO.errorResponse();
    }

    @RequestMapping("/getUserSequence")
    public ResponseVO getUserSequence(@RequestBody @Validated
                                      GetUserSequenceReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.getUserSequence(req);
    }

}
