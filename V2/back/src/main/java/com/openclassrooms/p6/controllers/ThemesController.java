package com.openclassrooms.p6.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.openclassrooms.p6.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.openclassrooms.p6.exception.ApiException;
import com.openclassrooms.p6.exception.GlobalExceptionHandler;
import com.openclassrooms.p6.mapper.SubscriptionMapper;
import com.openclassrooms.p6.mapper.ThemeMapper;
import com.openclassrooms.p6.model.Subscriptions;
import com.openclassrooms.p6.model.Themes;
import com.openclassrooms.p6.model.Users;
import com.openclassrooms.p6.payload.response.MessageResponse;
import com.openclassrooms.p6.payload.response.SingleThemeResponse;
import com.openclassrooms.p6.payload.response.SingleThemeSubscriptionResponse;
import com.openclassrooms.p6.service.SubscriptionsService;
import com.openclassrooms.p6.service.ThemeService;
import com.openclassrooms.p6.service.UserService;
import com.openclassrooms.p6.utils.JwtUtil;

@RestController
@RequestMapping("/api/themes")
public class ThemesController {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private SubscriptionsService subscriptionsService;

    @Autowired
    private SubscriptionMapper subscriptionsMapper;

    @Autowired
    private ThemeMapper themeMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;


    @GetMapping("")
    public ResponseEntity<?> getAllThemes(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            Long userId = authService.verifyUserValidityFromToken(authorizationHeader);

            List<Themes> themesEntityList = themeService.getThemes();
            Iterable<SingleThemeResponse> themesDto = themeMapper.toDtoThemes(themesEntityList);

            return ResponseEntity.status(HttpStatus.OK).body(themesDto);
        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }

    @GetMapping("/subscribed")
    public ResponseEntity<?> getSubscribedThemes(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            Long userId = authService.verifyUserValidityFromToken(authorizationHeader);

            Iterable<Subscriptions> subscriptions = subscriptionsService.findAllUserSubscriptions(userId);
            Iterable<SingleThemeSubscriptionResponse> subscriptionsDto = subscriptionsMapper.toDtoSubscriptions(subscriptions);

            return ResponseEntity.status(HttpStatus.OK).body(subscriptionsDto);
        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }

    @PostMapping("/subscribe/")
    public ResponseEntity<?> subscribe(
            @RequestParam final Long themeId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            Long userId = authService.verifyUserValidityFromToken(authorizationHeader);
            verifyAndGetThemeById(themeId);

            Subscriptions subscription = getUserThemeSubscription(userId, themeId);

            if (subscription == null) {
                subscriptionsService.createSubscription(userId, themeId);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new MessageResponse("Successfully subscribed to the theme!"));
            }

            if (subscription.getIsSubscribed()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("You are already subscribed to this theme!"));
            }

            subscriptionsService.updateThemeSubscription(subscription, true);
            return ResponseEntity.ok(new MessageResponse("Successfully subscribed to the theme!"));

        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }

    @PostMapping("/unsubscribe/")
    public ResponseEntity<?> unsubscribe(
            @RequestParam final Long themeId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            Long userId = authService.verifyUserValidityFromToken(authorizationHeader);
            verifyAndGetThemeById(themeId);

            Subscriptions subscription = getUserThemeSubscription(userId, themeId);

            if (subscription == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Subscription does not exist!"));
            }

            if (!subscription.getIsSubscribed()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("You cannot unsubscribe from a theme you are not subscribed to!"));
            }

            subscriptionsService.updateThemeSubscription(subscription, false);
            return ResponseEntity.ok(new MessageResponse("Successfully unsubscribed from the theme!"));

        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }

    // ----------------------- PRIVATE METHODS ----------------------------

    private Users getVerifiedUserById(Long userId) {
        return userService.getUserById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        List.of("User not found"),
                        HttpStatus.NOT_FOUND,
                        LocalDateTime.now()));
    }


    private Themes verifyAndGetThemeById(Long themeId) {
        return themeService.getThemeById(themeId)
                .orElseThrow(() -> new ApiException(
                        "Theme not found",
                        List.of("Theme not found"),
                        HttpStatus.NOT_FOUND,
                        LocalDateTime.now()));
    }


    private Subscriptions getUserThemeSubscription(Long userId, Long themeId) {
        Iterable<Subscriptions> subscriptions = subscriptionsService.findAllUserSubscriptions(userId);

        return StreamSupport.stream(subscriptions.spliterator(), false)
                .filter(s -> s.getThemeId().equals(themeId))
                .findFirst()
                .orElse(null);
    }
}
