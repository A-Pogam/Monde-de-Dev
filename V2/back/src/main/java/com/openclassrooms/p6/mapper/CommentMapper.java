package com.openclassrooms.p6.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.openclassrooms.p6.model.Comments;
import com.openclassrooms.p6.payload.response.CommentResponse;

/**
 * Interface responsible for mapping between {@link Comments} domain models and
 * {@link CommentResponse} data transfer objects.
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {


    @Mapping(target = "username", source = "user.username")
    CommentResponse toDtoComment(Comments comment);


    Iterable<CommentResponse> toDtoComments(List<Comments> comments);
}