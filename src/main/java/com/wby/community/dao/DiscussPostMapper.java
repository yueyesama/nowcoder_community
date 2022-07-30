package com.wby.community.dao;

import com.wby.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    //在查看 个人主页/我的帖子 时才用到userId
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);


    // @Param注解用于给参数取别名
    // 如果只有一个参数，并且在<if>里使用，则必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);





}
