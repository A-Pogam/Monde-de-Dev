package com.openclassrooms.p6.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.openclassrooms.p6.exception.ApiException;
import com.openclassrooms.p6.exception.GlobalExceptionHandler;
import com.openclassrooms.p6.mapper.UserMapper;
import com.openclassrooms.p6.model.Users;
import com.openclassrooms.p6.payload.request.UserRequest;
import com.openclassrooms.p6.payload.response.MessageResponse;
import com.openclassrooms.p6.payload.response.UserInfoResponse;
import com.openclassrooms.p6.service.UserService;
import com.openclassrooms.p6.utils.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;


    @GetMapping("")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        try {
            Long userId = Long.parseLong((String) authentication.getPrincipal());
            Users user = getVerifiedUserById(userId);
            UserInfoResponse response = userMapper.toDtoUser(user);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }

    @PutMapping("")
    public ResponseEntity<?> update(
            @Valid @RequestBody UserRequest request,
            BindingResult bindingResult,
            Authentication authentication) {
        try {
            if (bindingResult.hasErrors()) {
                GlobalExceptionHandler.handlePayloadError("Bad request", bindingResult, HttpStatus.BAD_REQUEST);
            }

            Long userId = Long.parseLong((String) authentication.getPrincipal());
            Users user = userService.getUserById(userId)
                    .orElseThrow(() -> new ApiException(
                            "User not found",
                            List.of("User with ID " + userId + " not found"),
                            HttpStatus.NOT_FOUND,
                            LocalDateTime.now()));

            if (request.email() != null && !request.email().isEmpty()) {
                Optional<Users> userFromRequestEmail = userService.getUserByEmail(request.email());
                if (userFromRequestEmail.isPresent()
                        && !userFromRequestEmail.get().getId().equals(user.getId())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new MessageResponse("The new email is already taken!"));
                }

                user.setEmail(request.email());
            }

            if (request.username() != null && !request.username().isEmpty()) {
                user.setUsername(request.username());
            }

            if (request.password() != null && !request.password().isEmpty()) {
                String encodedPassword = userService.encodePassword(request.password());
                user.setPassword(encodedPassword);
            }

            userService.saveUser(user);

            return ResponseEntity.ok(new MessageResponse("Successfully changed the user credentials!"));

        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }




    private void checkBodyPayloadErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            GlobalExceptionHandler.handlePayloadError("Bad request", bindingResult, HttpStatus.BAD_REQUEST);
        }
    }

    private Long extractUserIdFromJwt(String authorizationHeader) {
        String jwtToken = JwtUtil.extractJwtFromHeader(authorizationHeader);
        return jwtUtil.extractUserId(jwtToken)
                .orElseThrow(() -> new ApiException(
                        "Unauthorized",
                        List.of("Invalid or expired JWT"),
                        HttpStatus.UNAUTHORIZED,
                        LocalDateTime.now()));
    }

    private Users getVerifiedUserById(Long userId) {
        return userService.getUserById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        List.of("User with ID " + userId + " not found"),
                        HttpStatus.NOT_FOUND,
                        LocalDateTime.now()));
    }


}
