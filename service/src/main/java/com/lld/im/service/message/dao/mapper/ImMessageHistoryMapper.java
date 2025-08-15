package com.lld.im.service.message.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.message.dao.ImMessageHistoryEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * ClassName: ImMessageHistoryMapper
 * Package: com.lld.im.service.message.dao.mapper
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/15 上午9:06
 * Version 1.0
 */
@Repository
public interface ImMessageHistoryMapper extends BaseMapper<ImMessageHistoryEntity> {
    /**
     * 批量插入（mysql）
     * @param entityList
     * @return
     */
    Integer insertBatchSomeColumn(Collection<ImMessageHistoryEntity> entityList);
}
