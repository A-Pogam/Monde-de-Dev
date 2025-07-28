package com.openclassrooms.p6.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.openclassrooms.p6.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.openclassrooms.p6.exception.ApiException;
import com.openclassrooms.p6.exception.GlobalExceptionHandler;
import com.openclassrooms.p6.mapper.ArticleMapper;
import com.openclassrooms.p6.mapper.CommentMapper;
import com.openclassrooms.p6.model.Articles;
import com.openclassrooms.p6.model.Comments;
import com.openclassrooms.p6.model.Themes;
import com.openclassrooms.p6.model.Users;
import com.openclassrooms.p6.payload.request.ArticleRequest;
import com.openclassrooms.p6.payload.request.CommentRequest;
import com.openclassrooms.p6.payload.response.ArticleSummaryResponse;
import com.openclassrooms.p6.payload.response.CommentResponse;
import com.openclassrooms.p6.payload.response.MessageResponse;
import com.openclassrooms.p6.payload.response.MultipleArticlesResponse;
import com.openclassrooms.p6.payload.response.SingleArticleResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/articles")
public class ArticlesController {

    @Autowired private UserService userService;
    @Autowired private ArticleService articleService;
    @Autowired private CommentsService commentsService;
    @Autowired private ThemeService themeService;
    @Autowired private ArticleMapper articleMapper;
    @Autowired private CommentMapper commentsMapper;

    @GetMapping("")
    public ResponseEntity<?> getAllArticles(Authentication authentication) {
        try {
            Long userId = Long.parseLong((String) authentication.getPrincipal());
            List<Articles> articlesEntity = articleService.getArticles();
            List<ArticleSummaryResponse> articlesDto = new ArrayList<>();
            articleMapper.toDtoArticles(articlesEntity).forEach(articlesDto::add);
            MultipleArticlesResponse response = new MultipleArticlesResponse(articlesDto);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArticleById(@PathVariable("id") Long articleId, Authentication authentication) {
        try {
            Long userId = Long.parseLong((String) authentication.getPrincipal());
            Articles article = verifyAndGetArticleById(articleId);
            ArticleSummaryResponse articleDto = articleMapper.toDtoArticle(article);
            String author = getVerifiedUserById(article.getUserId()).getUsername();
            String theme = article.getTheme().getTitle();

            List<CommentResponse> comments = new ArrayList<>();
            commentsMapper.toDtoComments(commentsService.getAllCommentsByArticleId(articleId)).forEach(comments::add);

            SingleArticleResponse response = new SingleArticleResponse(
                    articleId, author, articleDto.publicationDate(),
                    theme, articleDto.title(), articleDto.description(), comments
            );
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }

    @PostMapping("")
    public ResponseEntity<?> postArticle(
            @RequestParam Long themeId,
            @Valid @RequestBody ArticleRequest request,
            BindingResult bindingResult,
            Authentication authentication) {
        try {
            System.out.println("AUTHENTICATION: " + authentication);
            System.out.println("PRINCIPAL: " + authentication.getPrincipal());
            System.out.println("AUTHORITIES: " + authentication.getAuthorities());

            Long userId = Long.parseLong((String) authentication.getPrincipal());
            checkBodyPayloadErrors(bindingResult);
            Themes theme = verifyOrCreateThemeById(themeId);
            articleService.createArticle(request, userId, theme.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Article has been successfully published !"));
        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }

    private Themes verifyOrCreateThemeById(Long themeId) {
        return themeService.getThemeById(themeId)
                .orElseGet(() -> {
                    // Créer un thème générique si l'ID est inconnu
                    Themes newTheme = new Themes();
                    newTheme.setTitle("Thème inconnu " + themeId); // ou mieux, envoyer le nom du thème dans le body
                    return themeService.createTheme(newTheme); // méthode à implémenter
                });
    }



    @PostMapping("/comment/")
    public ResponseEntity<?> postCommentToArticle(
            @RequestParam Long articleId,
            @Valid @RequestBody CommentRequest request,
            BindingResult bindingResult,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong((String) authentication.getPrincipal());
            checkBodyPayloadErrors(bindingResult);
            verifyAndGetArticleById(articleId);
            commentsService.createComment(request, userId, articleId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Comment has been successfully published !"));
        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }

    }

    // === PRIVATE HELPERS ===

    private Users getVerifiedUserById(Long userId) {
        return userService.getUserById(userId).orElseThrow(() ->
                new ApiException(
                        "Theme not found",
                        List.of("No theme with ID: " + userId),
                        HttpStatus.NOT_FOUND,
                        LocalDateTime.now()
                )
        );
    }



    private Articles verifyAndGetArticleById(Long articleId) {
        return articleService.getArticleById(articleId).orElseThrow(() ->
                new ApiException(
                        "Theme not found",
                        List.of("No theme with ID: " + articleId),
                        HttpStatus.NOT_FOUND,
                        LocalDateTime.now()
                )
        );
    }

    private Themes verifyAndGetThemeById(Long themeId) {
        return themeService.getThemeById(themeId).orElseThrow(() ->
                new ApiException(
                        "Theme not found",
                        List.of("No theme with ID: " + themeId),
                        HttpStatus.NOT_FOUND,
                        LocalDateTime.now()
                )
        );
    }

    private void checkBodyPayloadErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            GlobalExceptionHandler.handlePayloadError("Bad request", bindingResult, HttpStatus.BAD_REQUEST);
        }
    }
}
