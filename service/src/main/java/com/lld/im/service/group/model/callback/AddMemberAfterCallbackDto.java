package com.lld.im.service.group.model.callback;

import com.lld.im.service.group.model.resp.AddMemberResp;
import lombok.Data;

import java.util.List;

/**
 * ClassName: AddMemberAfterCallback
 * Package: com.lld.im.service.group.model.callback
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/6 上午9:09
 * Version 1.0
 */
@Data
public class AddMemberAfterCallbackDto {
    private String groupId;
    private Integer groupType;
    private String operater;
    private List<AddMemberResp> memberResps;
}
