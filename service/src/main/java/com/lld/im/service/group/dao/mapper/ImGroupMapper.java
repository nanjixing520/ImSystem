package com.lld.im.service.group.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.group.dao.ImGroupEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;

/**
 * ClassName: ImGroupMapper
 * Package: com.lld.im.service.group.dao.mapper
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/22 下午5:32
 * Version 1.0
 */
@Mapper
public interface ImGroupMapper extends BaseMapper<ImGroupEntity> {
    @Select(" <script> " +
            " select max(sequence) from im_group where app_id = #{appId} and group_id in " +
            "<foreach collection='groupId' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            " </script> ")
    Long getGroupMaxSeq(Collection<String> groupId, Integer appId);
}
