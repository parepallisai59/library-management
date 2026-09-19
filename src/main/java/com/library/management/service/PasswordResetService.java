package com.library.management.service;

import com.library.management.model.PasswordResetToken;
import com.library.management.model.User;
import com.library.management.repository.PasswordResetTokenRepository;
import com.library.management.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository               userRepository;
    private final PasswordEncoder              passwordEncoder;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a reset token for the given email.
     * Returns the token string so the caller can display / email it.
     * If the email is not registered, returns null (do NOT reveal this to the user
     * for security — always show the same "check your email" message).
     */
    @Transactional
    public String createToken(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return null;

        String token = UUID.randomUUID().toString();
        tokenRepository.save(new PasswordResetToken(
                token, email, LocalDateTime.now().plusHours(1)));
        return token;
    }

    /**
     * Validates the token and resets the password.
     * Returns a map with "success" (boolean) and "message" (string).
     */
    @Transactional
    public Map<String, Object> resetPassword(String token, String newPassword) {

        PasswordResetToken prt = tokenRepository.findByToken(token).orElse(null);

        if (prt == null) {
            return Map.of("success", false, "message", "Invalid or expired reset link.");
        }
        if (prt.isUsed()) {
            return Map.of("success", false, "message", "This link has already been used.");
        }
        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Map.of("success", false, "message", "Reset link has expired. Please request a new one.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Map.of("success", false, "message", "Password must be at least 6 characters.");
        }

        User user = userRepository.findByEmail(prt.getEmail()).orElse(null);
        if (user == null) {
            return Map.of("success", false, "message", "User not found.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        prt.setUsed(true);
        tokenRepository.save(prt);

        return Map.of("success", true, "message", "Password updated successfully. You can now log in.");
    }
}
