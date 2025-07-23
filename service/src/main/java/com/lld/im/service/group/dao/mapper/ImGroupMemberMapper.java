package com.lld.im.service.group.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.common.ResponseVO;
import com.lld.im.service.group.dao.ImGroupMemberEntity;
import com.lld.im.service.group.model.req.GetJoinedGroupReq;
import com.lld.im.service.group.model.req.GroupMemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * ClassName: ImGroupMemberMapper
 * Package: com.lld.im.service.group.dao.mapper
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/22 下午5:34
 * Version 1.0
 */
@Mapper
public interface ImGroupMemberMapper extends BaseMapper<ImGroupMemberEntity> {
//将查询结果集的列与实体类的属性建立映射关系
    @Results({
            @Result(column = "member_id", property = "memberId"),
//            @Result(column = "speak_flag", property = "speakFlag"),
            @Result(column = "speak_date", property = "speakDate"),
            @Result(column = "role", property = "role"),
            @Result(column = "alias", property = "alias"),
            @Result(column = "join_time", property = "joinTime"),
            @Result(column = "join_type", property = "joinType")
    })
    @Select("select " +
            " member_id, " +
//            " speak_flag,  " +
            " speak_date,  " +
            " role, " +
            " alias, " +
            " join_time ," +
            " join_type " +
            " from im_group_member where app_id = #{appId} AND group_id = #{groupId} ")
    public List<GroupMemberDto> getGroupMember(Integer appId, String groupId);
    @Select("select group_id as groupId from im_group_member where member_id=#{memberId} and app_id=#{appId}")
    public List<String> getJoinedGroupId(Integer appId, String memberId);
}
