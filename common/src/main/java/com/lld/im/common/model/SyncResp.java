package com.lld.im.common.model;

import lombok.Data;

import java.util.List;

/**
 * ClassName: SyncResp
 * Package: com.lld.im.common.model
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/22 上午9:33
 * Version 1.0
 */
@Data
public class SyncResp<T> {
    //服务端最大的seq
    private Long maxSequence;
    //是否全部增量拉取完
    private boolean isCompleted;
    //拉取的数据
    private List<T> dataList;

}
