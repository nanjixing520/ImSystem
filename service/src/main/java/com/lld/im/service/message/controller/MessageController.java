package com.lld.im.service.message.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SyncReq;
import com.lld.im.common.model.message.CheckSendMessageReq;
import com.lld.im.service.message.model.req.SendMessageReq;
import com.lld.im.service.message.service.MessageSyncService;
import com.lld.im.service.message.service.P2PMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: MessageController
 * Package: com.lld.im.service.message.controller
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/15 下午2:47
 * Version 1.0
 */
@RestController
@RequestMapping("v1/message")
public class MessageController {
    @Autowired
    P2PMessageService p2PMessageService;
    @Autowired
    MessageSyncService messageSyncService;

    @RequestMapping("/send")
    public ResponseVO send(@RequestBody @Validated SendMessageReq req, Integer appId)  {
        req.setAppId(appId);
        return ResponseVO.successResponse(p2PMessageService.send(req));
    }

    /**
     * 供im服务调用的内部消息前置校验接口，配置拦截器的时候不拦截此请求
     * @param req
     * @return
     */
    @RequestMapping("/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated CheckSendMessageReq req)  {
        return p2PMessageService.imServerPermissionCheck(req.getFromId(),req.getToId(),req.getAppId());
    }
    @RequestMapping("/syncOfflineMessage")
    public ResponseVO syncOfflineMessage(@RequestBody
                                         @Validated SyncReq req, Integer appId)  {
        req.setAppId(appId);
        return messageSyncService.syncOfflineMessage(req);
    }
}
