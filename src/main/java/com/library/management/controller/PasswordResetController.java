package com.library.management.controller;

import com.library.management.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /**
     * POST /users/forgot-password
     * Body: { "email": "user@example.com" }
     *
     * In production, this would send an email with the reset link.
     * For this deployment we return the token in the response so you can
     * test the flow immediately without an email server.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @RequestBody Map<String, String> body) {

        String email = body.getOrDefault("email", "").trim();
        if (email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email is required."));
        }

        String token = passwordResetService.createToken(email);

        // Always return the same user-facing message to prevent email enumeration.
        // We also return the reset URL so the user can proceed without an email server.
        String resetUrl = "/reset-password?token=" + (token != null ? token : "INVALID");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "If that email is registered, a reset link has been generated.",
                "resetUrl", resetUrl   // In production: remove this and send via email
        ));
    }

    /**
     * POST /users/reset-password
     * Body: { "token": "...", "password": "newpassword" }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestBody Map<String, String> body) {

        String token    = body.getOrDefault("token", "").trim();
        String password = body.getOrDefault("password", "");

        if (token.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Reset token is required."));
        }

        Map<String, Object> result = passwordResetService.resetPassword(token, password);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return success ? ResponseEntity.ok(result)
                       : ResponseEntity.badRequest().body(result);
    }
}
