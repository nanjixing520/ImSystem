package com.lld.im.tcp.feign;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.message.CheckSendMessageReq;
import feign.Headers;
import feign.RequestLine;

/**
 * ClassName: FeignMessageService
 * Package: com.lld.im.tcp.feign
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/15 下午11:28
 * Version 1.0
 */
public interface FeignMessageService {
    @Headers({"Content-Type: application/json","Accept: application/json"})
    @RequestLine("POST /message/checkSend")
    public ResponseVO checkSendMessage(CheckSendMessageReq o);
    @Headers({"Content-Type: application/json","Accept: application/json"})
    @RequestLine("POST /group/checkSend")
    public ResponseVO checkSendGroupMessage(CheckSendMessageReq o);

}
