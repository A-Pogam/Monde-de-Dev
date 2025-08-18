package com.openclassrooms.p6.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.openclassrooms.p6.model.Users;
import com.openclassrooms.p6.payload.request.RegisterRequest;
import com.openclassrooms.p6.repository.UserRepository;

import lombok.Data;

/**
 * Service class for managing users.
 */
@Data
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;

    public Optional<Users> getUserById(final Long id) {
        return userRepository.findById(id);
    }

    public Iterable<Users> getUsers() {
        return userRepository.findAll();
    }

    public Users saveUser(Users user) {
        return userRepository.save(user);
    }

    public void deleteUser(final Long id) {
        userRepository.deleteById(id);
    }

    public Optional<Users> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean isUsernameInUse(String username) {
        Optional<Users> existingUser = getUserByUsername(username);
        return existingUser.isPresent();
    }

    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean isEmailInUse(String email) {
        Optional<Users> existingUser = getUserByEmail(email);
        return existingUser.isPresent();
    }

    public boolean isPasswordValid(String password, Users user) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    public Users saveUserBySignUp(RegisterRequest registrationRequest) {
        Users user = new Users();

        LocalDateTime currentTime = LocalDateTime.now();
        String encodedPassword = passwordEncoder.encode(registrationRequest.password());

        user.setUsername(registrationRequest.username());
        user.setEmail(registrationRequest.email());
        user.setPassword(encodedPassword);
        user.setCreatedAt(currentTime);
        user.setUpdatedAt(currentTime);

        return saveUser(user);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
    
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        Users user = getUserById(userId).orElseThrow(() ->
                new RuntimeException("User not found")
        );

        user.setPassword(encodePassword(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        saveUser(user);
    }
}
