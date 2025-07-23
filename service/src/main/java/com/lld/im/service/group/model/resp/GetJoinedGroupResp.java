package com.lld.im.service.group.model.resp;

import com.lld.im.service.group.dao.ImGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * ClassName: GetJoinedGroupResp
 * Package: com.lld.im.service.group.model.resp
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/23 上午10:29
 * Version 1.0
 */
@Data
public class GetJoinedGroupResp {

    private Integer totalCount;

    private List<ImGroupEntity> groupList;

}

