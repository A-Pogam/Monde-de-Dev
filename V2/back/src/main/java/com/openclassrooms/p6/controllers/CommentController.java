package com.openclassrooms.p6.controllers;

import com.openclassrooms.p6.mapper.CommentMapper;
import com.openclassrooms.p6.model.Comments;
import com.openclassrooms.p6.payload.response.CommentResponse;
import com.openclassrooms.p6.service.CommentsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentsService commentsService;
    private final CommentMapper commentMapper;

    public CommentController(CommentsService commentsService, CommentMapper commentMapper) {
        this.commentsService = commentsService;
        this.commentMapper = commentMapper;
    }

    /**
     * GET /comments?articleId=123
     */
    @GetMapping
    public Iterable<CommentResponse> getCommentsByArticle(@RequestParam Long articleId) {
        List<Comments> comments = commentsService.getAllCommentsByArticleId(articleId);
        return commentMapper.toDtoComments(comments);
    }
}
