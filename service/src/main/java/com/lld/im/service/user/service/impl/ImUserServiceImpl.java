package com.lld.im.service.user.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.codec.pack.user.UserModifyPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.enums.UserErrorCode;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.service.group.service.ImGroupService;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.dao.mapper.ImUserDataMapper;
import com.lld.im.service.user.model.req.*;
import com.lld.im.service.user.model.resp.GetUserInfoResp;
import com.lld.im.service.user.model.resp.ImportUserResp;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.CallbackService;
import com.lld.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: ImUserServiceImpl
 * Package: com.lld.im.service.user.service.impl
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/17 下午4:19
 * Version 1.0
 */
@Service
public class ImUserServiceImpl implements ImUserService {
    @Autowired
    private ImUserDataMapper imUserDataMapper;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private CallbackService callbackService;
    @Autowired
    private MessageProducer messageProducer;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ImGroupService imGroupService;
    @Override
    public ResponseVO importUser(ImportUserReq req) {
        if(req.getUserData().size()>100){
            //TODO:返回数量太多
            return ResponseVO.errorResponse(UserErrorCode.IMPORT_SIZE_BEYOND);
        }
        //存导入成功用户的id
        List<String> successId= new ArrayList<>();
        //存导入失败用户的id
        List<String> errorId= new ArrayList<>();
        //创建返回导入成功和失败的用户id的类
        ImportUserResp importUserResp = new ImportUserResp();
        //遍历请求导入用户列表
        req.getUserData().forEach(e->{
            try{
                //不要求用户自己传递appId值，严格按照本身的appId去设置
                e.setAppId(req.getAppId());
                int insert = imUserDataMapper.insert(e);
                if(insert==1){
                    successId.add(e.getUserId());
                }
            }catch(Exception ex){
                ex.printStackTrace();
                errorId.add(e.getUserId());
            }

        });
        importUserResp.setSuccessId(successId);
        importUserResp.setErrorId(errorId);
        return ResponseVO.successResponse(importUserResp);
    }

    @Override
    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req) {
        QueryWrapper<ImUserDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id",req.getAppId());
        queryWrapper.in("user_id",req.getUserIds());
        queryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        List<ImUserDataEntity> userDataEntities = imUserDataMapper.selectList(queryWrapper);
        HashMap<String, ImUserDataEntity> map = new HashMap<>();

        for (ImUserDataEntity data:
                userDataEntities) {
            map.put(data.getUserId(),data);
        }

        List<String> failUser = new ArrayList<>();
        for (String uid:
                req.getUserIds()) {
            if(!map.containsKey(uid)){
                failUser.add(uid);
            }
        }

        GetUserInfoResp resp = new GetUserInfoResp();
        resp.setUserDataItem(userDataEntities);
        resp.setFailUser(failUser);
        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId, Integer appId) {
        QueryWrapper objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("app_id",appId);
        objectQueryWrapper.eq("user_id",userId);
        objectQueryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        ImUserDataEntity ImUserDataEntity = imUserDataMapper.selectOne(objectQueryWrapper);
        if(ImUserDataEntity == null){
            return ResponseVO.errorResponse(UserErrorCode.USER_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(ImUserDataEntity);
    }

    @Override
    public ResponseVO deleteUser(DeleteUserReq req) {
        ImUserDataEntity entity = new ImUserDataEntity();
        entity.setDelFlag(DelFlagEnum.DELETE.getCode());
        List<String> errorId = new ArrayList();
        List<String> successId = new ArrayList();
        for (String userId:
                req.getUserId()) {
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.eq("app_id",req.getAppId());
            wrapper.eq("user_id",userId);
            wrapper.eq("del_flag",DelFlagEnum.NORMAL.getCode());
            int update = 0;

            try {
                update =  imUserDataMapper.update(entity, wrapper);
                if(update > 0){
                    successId.add(userId);
                }else{
                    errorId.add(userId);
                }
            }catch (Exception e){
                errorId.add(userId);
            }
        }

        ImportUserResp resp = new ImportUserResp();
        resp.setSuccessId(successId);
        resp.setErrorId(errorId);
        return ResponseVO.successResponse(resp);
    }

    @Override
    @Transactional
    public ResponseVO modifyUserInfo(ModifyUserInfoReq req) {
        QueryWrapper query = new QueryWrapper<>();
        query.eq("app_id",req.getAppId());
        query.eq("user_id",req.getUserId());
        query.eq("del_flag",DelFlagEnum.NORMAL.getCode());
        ImUserDataEntity user = imUserDataMapper.selectOne(query);
        if(user == null){
            throw new ApplicationException(UserErrorCode.USER_IS_NOT_EXIST);
        }

        ImUserDataEntity update = new ImUserDataEntity();
        BeanUtils.copyProperties(req,update);

        update.setAppId(null);
        update.setUserId(null);
        int update1 = imUserDataMapper.update(update, query);
        if(update1 == 1){
            //TCP通知：为了多端同步，发起方来源于后台管理员/app某一客户端
            UserModifyPack userModifyPack = new UserModifyPack();
            BeanUtils.copyProperties(req,userModifyPack);
            messageProducer.sendToUser(req.getUserId(),req.getClientType(),req.getImei(),
                    UserEventCommand.USER_MODIFY,userModifyPack,req.getAppId());
            //更改用户信息之后回调
            if(appConfig.isModifyUserAfterCallback()){
                //修改了什么就发送什么，把请求体中的数据都发送过去
                callbackService.callback(req.getAppId(), Constants.CallbackCommand.ModifyUserAfter, JSONObject.toJSONString(req));
            }
            return ResponseVO.successResponse();
        }
        throw new ApplicationException(UserErrorCode.MODIFY_USER_ERROR);
    }

    @Override
    public ResponseVO login(LoginReq req) {
        return ResponseVO.successResponse();
    }

    /**
     * 获得该用户在服务端的各数据最大序列号
     * 注意:
     *  用户的各数据最大seq已经写入到了redis中进行维护，可以直接从redis中进行获取，
     *  但是群组没有写入，只能通过实时查询数据库获取
     * @param req
     * @return
     */
    @Override
    public ResponseVO getUserSequence(GetUserSequenceReq req) {
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(req.getAppId() + ":" + Constants.RedisConstants.SeqPrefix + ":" + req.getUserId());
        Long groupSeq = imGroupService.getUserGroupMaxSeq(req.getUserId(),req.getAppId());
        map.put(Constants.SeqConstants.Group,groupSeq);
        return ResponseVO.successResponse(map);
    }
}
