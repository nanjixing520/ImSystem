package com.lld.im.service.friendship.model.resp;

import lombok.Data;

import java.util.List;

/**
 * ClassName: ImportFriendShipResp
 * Package: com.lld.im.service.friendship.model.resp
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/19 上午11:24
 * Version 1.0
 */
@Data
public class ImportFriendShipResp {
    private List<String> successId;
    private List<String> errorId;
}
