package com.openclassrooms.p6.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.p6.model.Articles;
import com.openclassrooms.p6.payload.request.ArticleRequest;
import com.openclassrooms.p6.repository.ArticleRepository;

import lombok.Data;

@Data
@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;


    public List<Articles> getArticles() {
        return articleRepository.findAll();
    }


    public Optional<Articles> getArticleById(final Long id) {
        return articleRepository.findById(id);
    }


    public Articles createArticle(ArticleRequest articleCreationRequest, Long userId, Long themeId) {
        Articles article = new Articles();

        article.setUserId(userId);
        article.setThemeId(themeId);
        article.setTitle(articleCreationRequest.title());
        article.setDescription(articleCreationRequest.description());
        article.setContent(articleCreationRequest.content());


        return articleRepository.save(article);
    }


    public void deleteArticleById(final Long id) {
        articleRepository.deleteById(id);
    }

}
