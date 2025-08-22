package com.lld.im.service.user.model.resp;

import com.lld.im.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * ClassName: UserOnlineStatusResp
 * Package: com.lld.im.service.user.model.resp
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/23 上午1:38
 * Version 1.0
 */
@Data
public class UserOnlineStatusResp {
    // 在服务端真实的在线状态
    // 用户各在线/下线端信息集合
    // 集合为空就是下线，集合中有任意一个客户端的在线状态是在线那就是该用户在线（交给客户端判断）
    private List<UserSession> session;
    //以下为自定义的客户端状态
    private String customText;

    private Integer customStatus;

}
