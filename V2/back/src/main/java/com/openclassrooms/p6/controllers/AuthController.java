package com.openclassrooms.p6.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.openclassrooms.p6.exception.ApiException;
import com.openclassrooms.p6.exception.GlobalExceptionHandler;
import com.openclassrooms.p6.mapper.UserMapper;
import com.openclassrooms.p6.model.Users;
import com.openclassrooms.p6.payload.request.LoginRequest;
import com.openclassrooms.p6.payload.request.RegisterRequest;
import com.openclassrooms.p6.payload.response.AuthResponse;
import com.openclassrooms.p6.payload.response.UserInfoResponse;
import com.openclassrooms.p6.service.UserService;
import com.openclassrooms.p6.utils.JwtUtil;

import jakarta.validation.Valid;

/**
 * Controller for authentication: register and login.
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Registers a new user and returns a JWT.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                      BindingResult bindingResult) {
        try {
            checkBodyPayloadErrors(bindingResult);
            checkIfUsernameIsInUse(request.username());
            checkIfEmailIsInUse(request.email());

            Users user = userService.saveUserBySignUp(request);
            UserInfoResponse userDto = userMapper.toDtoUser(user);

            String jwtToken = jwtUtil.generateJwtToken(userDto.id());

            AuthResponse authResponse = new AuthResponse(jwtToken, userDto.id(), userDto.username(), userDto.email());
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);

        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }

    /**
     * Logs in an existing user and returns a JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   BindingResult bindingResult) {
        try {
            checkBodyPayloadErrors(bindingResult);

            Users user = getUserFromIdentifier(request.identifier());
            checkUserPassword(request.password(), user);

            UserInfoResponse userDto = userMapper.toDtoUser(user);
            String jwtToken = jwtUtil.generateJwtToken(userDto.id());

            AuthResponse authResponse = new AuthResponse(jwtToken, userDto.id(), userDto.username(), userDto.email());
            return ResponseEntity.status(HttpStatus.OK).body(authResponse);

        } catch (ApiException e) {
            return GlobalExceptionHandler.handleApiException(e);
        }
    }


    private void checkBodyPayloadErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            GlobalExceptionHandler.handlePayloadError("Bad request", bindingResult, HttpStatus.BAD_REQUEST);
        }
    }

    private void checkIfUsernameIsInUse(String username) {
        if (userService.isUsernameInUse(username)) {
            GlobalExceptionHandler.handleLogicError("Username is already in use", HttpStatus.CONFLICT);
        }
    }

    private void checkIfEmailIsInUse(String email) {
        if (userService.isEmailInUse(email)) {
            GlobalExceptionHandler.handleLogicError("Email is already in use", HttpStatus.CONFLICT);
        }
    }

    private void checkUserPassword(String requestPassword, Users user) {
        if (!userService.isPasswordValid(requestPassword, user)) {
            GlobalExceptionHandler.handleLogicError("Password is incorrect", HttpStatus.UNAUTHORIZED);
        }
    }

    private Users getUserFromIdentifier(String identifier) {
        Optional<Users> userFromEmail = userService.getUserByEmail(identifier);
        Optional<Users> userFromUsername = userService.getUserByUsername(identifier);

        if (userFromEmail.isEmpty() && userFromUsername.isEmpty()) {
            GlobalExceptionHandler.handleLogicError("Invalid username/email", HttpStatus.UNAUTHORIZED);
        }

        return userFromEmail.orElseGet(userFromUsername::get);
    }
}
