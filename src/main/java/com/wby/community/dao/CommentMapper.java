package com.wby.community.dao;

import com.wby.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);

    List<Comment> selectCommentByUserIdAndEntityType(int userId, int entityType, int offset, int limit);

    int selectCommentCountByUserIdAndEntityType(int userId, int entityType);
}
