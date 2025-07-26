package com.openclassrooms.p6.service;

import com.openclassrooms.p6.exception.ApiException;
import com.openclassrooms.p6.model.Users;
import com.openclassrooms.p6.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    public Long verifyUserValidityFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ApiException(
                    "Authorization header is missing or malformed",
                    List.of("Missing or malformed token"),
                    HttpStatus.UNAUTHORIZED,
                    LocalDateTime.now()
            );
        }

        String jwtToken = JwtUtil.extractJwtFromHeader(authorizationHeader);

        if (!jwtUtil.isTokenValid(jwtToken)) {
            throw new ApiException(
                    "Invalid JWT token",
                    List.of("Token signature or format is invalid"),
                    HttpStatus.UNAUTHORIZED,
                    LocalDateTime.now()
            );
        }

        Long userId = jwtUtil.extractUserId(jwtToken).orElseThrow(() -> new ApiException(
                "Invalid JWT token",
                List.of("Could not extract user ID from token"),
                HttpStatus.UNAUTHORIZED,
                LocalDateTime.now()
        ));

        Optional<Users> optionalUser = userService.getUserById(userId);
        System.out.println("Token utilisé : " + jwtToken);
        System.out.println("Subject : " + jwtUtil.extractUserId(jwtToken));

        return optionalUser.map(Users::getId).orElseThrow(() -> new ApiException(
                "User not found",
                List.of("No user found with this ID"),
                HttpStatus.UNAUTHORIZED,
                LocalDateTime.now()
        ));
    }
}
