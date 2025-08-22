package com.lld.im.common.model;

import lombok.Data;

/**
 * ClassName: SyncReq
 * Package: com.lld.im.common.model
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/22 上午9:31
 * Version 1.0
 */
@Data
public class SyncReq extends RequestBase {

    //客户端最大seq
    private Long lastSequence;
    //一次拉取多少
    private Integer maxLimit;

}
