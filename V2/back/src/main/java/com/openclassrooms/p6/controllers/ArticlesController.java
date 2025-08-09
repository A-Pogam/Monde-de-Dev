package com.openclassrooms.p6.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

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
import com.openclassrooms.p6.payload.response.*;

import com.openclassrooms.p6.service.*;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ArticlesController {

    @Autowired private UserService userService;
    @Autowired private ArticleService articleService;
    @Autowired private CommentsService commentsService;
    @Autowired private ThemeService themeService;
    @Autowired private SubscriptionsService subscriptionsService;
    @Autowired private ArticleMapper articleMapper;
    @Autowired private CommentMapper commentsMapper;

    @GetMapping("")
    public ResponseEntity<?> getAllArticles(Authentication authentication) {
        try {
            Long userId = Long.parseLong((String) authentication.getPrincipal());

            List<Articles> allArticles = articleService.getArticles();

            // Filtrer selon les abonnements
            List<Long> subscribedThemeIds = StreamSupport.stream(
                            subscriptionsService.findAllUserSubscriptions(userId).spliterator(), false)
                    .filter(s -> Boolean.TRUE.equals(s.getIsSubscribed()))
                    .map(s -> s.getThemeId())
                    .toList();

            List<Articles> visibleArticles = allArticles.stream()
                    .filter(article -> subscribedThemeIds.contains(article.getThemeId()))
                    .toList();

            List<ArticleSummaryResponse> articlesDto = new ArrayList<>();
            articleMapper.toDtoArticles(visibleArticles).forEach(articlesDto::add);

            return ResponseEntity.ok(new MultipleArticlesResponse(articlesDto));

        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArticleById(@PathVariable("id") Long articleId, Authentication authentication) {
        try {
            Long userId = Long.parseLong((String) authentication.getPrincipal());

            Articles article = verifyAndGetArticleById(articleId);

            // Vérifier si l'utilisateur est abonné au thème de l'article
            boolean isSubscribed = subscriptionsService.isUserSubscribedToTheme(userId, article.getThemeId());
            if (!isSubscribed) {
                throw new ApiException(
                        "Access denied",
                        List.of("You must be subscribed to this theme to view the article."),
                        HttpStatus.FORBIDDEN,
                        LocalDateTime.now()
                );
            }

            ArticleSummaryResponse articleDto = articleMapper.toDtoArticle(article);
            String author = getVerifiedUserById(article.getUserId()).getUsername();
            String theme = article.getTheme().getTitle();

            List<CommentResponse> comments = new ArrayList<>();
            commentsMapper.toDtoComments(commentsService.getAllCommentsByArticleId(articleId)).forEach(comments::add);

            return ResponseEntity.ok(new SingleArticleResponse(
                    articleId, author, articleDto.publicationDate(),
                    theme, articleDto.title(), articleDto.description(), comments
            ));
        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }

    @PostMapping("")
    public ResponseEntity<?> postArticle(
            @Valid @RequestBody ArticleRequest request,
            BindingResult bindingResult,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong((String) authentication.getPrincipal());
            checkBodyPayloadErrors(bindingResult);

            Themes theme = verifyOrCreateThemeByTitle(request.title());
            articleService.createArticle(request, userId, theme.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Article has been successfully published!"));
        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
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

            Articles article = verifyAndGetArticleById(articleId);

            // Vérification de l’abonnement
            boolean isSubscribed = subscriptionsService.isUserSubscribedToTheme(userId, article.getThemeId());
            if (!isSubscribed) {
                throw new ApiException(
                        "Access denied",
                        List.of("You must be subscribed to this theme to comment on this article."),
                        HttpStatus.FORBIDDEN,
                        LocalDateTime.now()
                );
            }

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
                        "User not found",
                        List.of("No user with ID: " + userId),
                        HttpStatus.NOT_FOUND,
                        LocalDateTime.now()
                )
        );
    }

    private Articles verifyAndGetArticleById(Long articleId) {
        return articleService.getArticleById(articleId).orElseThrow(() ->
                new ApiException(
                        "Article not found",
                        List.of("No article with ID: " + articleId),
                        HttpStatus.NOT_FOUND,
                        LocalDateTime.now()
                )
        );
    }

    private Themes verifyOrCreateThemeByTitle(String themeTitle) {
        return themeService.getThemes().stream()
                .filter(theme -> theme.getTitle().equalsIgnoreCase(themeTitle))
                .findFirst()
                .orElseGet(() -> {
                    Themes newTheme = new Themes();
                    newTheme.setTitle(themeTitle);
                    return themeService.createTheme(newTheme);
                });
    }



    private void checkBodyPayloadErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            GlobalExceptionHandler.handlePayloadError("Bad request", bindingResult, HttpStatus.BAD_REQUEST);
        }
    }
}
