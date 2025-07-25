package com.lld.im.service.group.model.resp;

import lombok.Data;

/**
 * ClassName: AddMemberResp
 * Package: com.lld.im.service.group.model.resp
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/22 下午7:03
 * Version 1.0
 */
@Data
public class AddMemberResp {

    private String memberId;

    // 加人结果：0 为成功；1 为失败；2 为已经是群成员
    private Integer result;

    private String resultMessage;
}
