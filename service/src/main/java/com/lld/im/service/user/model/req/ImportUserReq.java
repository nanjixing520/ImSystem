package com.lld.im.service.user.model.req;

import com.lld.im.common.model.RequestBase;
import com.lld.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;

/**
 * ClassName: ImportUserReq
 * Package: com.lld.im.service.user.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/17 下午4:44
 * Version 1.0
 */
@Data
public class ImportUserReq extends RequestBase {
    /***
     * 批量的用户
     */
    private List<ImUserDataEntity> userData;
}
