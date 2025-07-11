package com.openclassrooms.p6.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.openclassrooms.p6.model.Articles;
import com.openclassrooms.p6.payload.response.ArticleSummaryResponse;

/**
 * Interface defining methods for mapping between {@link Articles} and
 * {@link ArticleSummaryResponse}.
 */
@Mapper(componentModel = "spring")
public interface ArticleMapper {


    @Mappings({
            @Mapping(target = "id", ignore = false),
            @Mapping(target = "publicationDate", source = "createdAt"),
            @Mapping(target = "username", source = "user.username"),
            @Mapping(target = "articleId", source = "id")
    })
    ArticleSummaryResponse toDtoArticle(Articles article);


    Iterable<ArticleSummaryResponse> toDtoArticles(List<Articles> articles);
}