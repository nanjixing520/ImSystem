package com.lld.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.GateWayErrorCode;
import com.lld.im.common.enums.ImUserTypeEnum;
import com.lld.im.common.exception.ApplicationExceptionEnum;
import com.lld.im.common.utils.SigAPI;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.service.ImUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @description:
 *   签名校验工具类：专门负责解析、校验 UserSig
 * @author: 南极星
 * @version: 1.0
 */
@Component
public class IdentityCheck {

    private static Logger logger = LoggerFactory.getLogger(IdentityCheck.class);

    @Autowired
    ImUserService imUserService;

    //10000 123456 10001 123456789
    //不同的app密钥不同，先写在配置文件中
    @Autowired
    AppConfig appConfig;
    //如果签名刚校验过，直接存在redis中，下次直接从Redis中读结果，不用重复解密
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public ApplicationExceptionEnum checkUserSig(String identifier,
                                                 String appId, String userSig){

        String cacheUserSig = stringRedisTemplate.opsForValue()
                .get(appId + ":" + Constants.RedisConstants.userSign + ":"
                        + identifier + userSig);
        if(!StringUtils.isBlank(cacheUserSig) && Long.valueOf(cacheUserSig)
                >  System.currentTimeMillis() / 1000 ){
            return BaseErrorCode.SUCCESS;
        }

        //获取秘钥
        String privateKey = appConfig.getPrivateKey();

        //根据appid + 秘钥创建sigApi
        SigAPI sigAPI = new SigAPI(Long.valueOf(appId), privateKey);

        //调用sigApi对userSig解密
        JSONObject jsonObject = sigAPI.decodeUserSig(userSig);

        //取出解密后的appid 和 操作人 和 过期时间做匹配，不通过则提示错误
        Long expireTime = 0L;//到期时间（具体的点单位秒）
        Long expireSec = 0L;//过期时间（时间段单位秒）
        String decoerAppId = "";
        String decoderidentifier = "";

        try {
            decoerAppId = jsonObject.getString("TLS.appId");
            decoderidentifier = jsonObject.getString("TLS.identifier");
            String expireStr = jsonObject.get("TLS.expire").toString();//过期时间（时间段单位毫秒）
            String expireTimeStr = jsonObject.get("TLS.expireTime").toString();//生成密钥的那个时刻，不是此时此刻（具体的点，单位秒）
            expireSec = Long.valueOf(expireStr)/1000 ;//过期时间（时间段单位秒）
            expireTime = Long.valueOf(expireTimeStr) + expireSec;//到期时间（具体的点单位秒）
        }catch (Exception e){
            e.printStackTrace();
            logger.error("checkUserSig-error:{}",e.getMessage());
        }

        if(!decoderidentifier.equals(identifier)){
            return GateWayErrorCode.USERSIGN_OPERATE_NOT_MATE;
        }

        if(!decoerAppId.equals(appId)){
            return GateWayErrorCode.USERSIGN_IS_ERROR;
        }

        if(expireSec == 0L){
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }


        if(expireTime < System.currentTimeMillis() / 1000 ){
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        //appid + "xxx" + userId + sign

        String key = appId + ":" + Constants.RedisConstants.userSign + ":"
                +identifier + userSig;

        Long etime = expireTime - System.currentTimeMillis() / 1000;//此时用户访问时距离过期剩下的时间段（单位秒），相当于redis中的存活时间TTL
        stringRedisTemplate.opsForValue().set(
                key,expireTime.toString(),etime, TimeUnit.SECONDS
        );
        return BaseErrorCode.SUCCESS;
    }


    /**
     * 根据appid,identifier判断是否App管理员,并设置到RequestHolder
     * @param identifier
     * @param appId
     * @return
     */
    public void setIsAdmin(String identifier, Integer appId) {
        //去DB或Redis中查找, 后面写
        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(identifier, appId);
        if(singleUserInfo.isOk()){
            RequestHolder.set(singleUserInfo.getData().getUserType() == ImUserTypeEnum.APP_ADMIN.getCode());
        }else{
            RequestHolder.set(false);
        }
    }
}
