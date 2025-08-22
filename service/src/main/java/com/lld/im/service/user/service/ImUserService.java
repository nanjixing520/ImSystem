package com.lld.im.service.user.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.model.req.*;
import com.lld.im.service.user.model.resp.GetUserInfoResp;

/**
 * ClassName: ImUserService
 * Package: com.lld.im.service.user.service
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/17 下午4:19
 * Version 1.0
 */
public interface ImUserService {
    /**
     * 批量导入用户
     * @param req
     * @return
     */
    public ResponseVO importUser(ImportUserReq req);

    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId , Integer appId);

    public ResponseVO deleteUser(DeleteUserReq req);

    public ResponseVO modifyUserInfo(ModifyUserInfoReq req);

    public ResponseVO login(LoginReq req);

    public ResponseVO getUserSequence(GetUserSequenceReq req);
}
