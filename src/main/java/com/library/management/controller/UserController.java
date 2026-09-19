package com.library.management.controller;

import com.library.management.dto.LoginResponse;
import com.library.management.dto.RegisterResponse;
import com.library.management.dto.UserResponse;
import com.library.management.model.User;
import com.library.management.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    // -------------------------------------------------------
    // REGISTER  — role is always USER; cannot self-promote
    // -------------------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody User user) {

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new RegisterResponse(false, "Name is required", null));
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new RegisterResponse(false, "Email is required", null));
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new RegisterResponse(false, "Password is required", null));
        }

        // Force role to USER — no self-promotion to ADMIN
        user.setRole("USER");

        if (userService.getUserByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RegisterResponse(false, "Email already registered", null));
        }

        User registered = userService.registerUser(user);
        if (registered == null) {
            return ResponseEntity.internalServerError()
                    .body(new RegisterResponse(false, "Registration failed", null));
        }

        UserResponse ur = new UserResponse(
                registered.getId(), registered.getName(),
                registered.getEmail(), registered.getRole());

        return ResponseEntity.ok(new RegisterResponse(true, "Registration successful", ur));
    }

    // -------------------------------------------------------
    // LOGIN  — returns 401 on bad credentials, not 500
    // -------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestBody User user,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(), user.getPassword()));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            User loggedInUser = userService.getUserByEmail(user.getEmail()).orElse(null);
            if (loggedInUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not found after authentication."));
            }

            return ResponseEntity.ok(new LoginResponse(
                    loggedInUser.getId(), loggedInUser.getName(),
                    loggedInUser.getEmail(), loggedInUser.getRole()));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password."));
        }
    }

    // -------------------------------------------------------
    // LIST ALL USERS  — ADMIN only (enforced in SecurityConfig)
    // -------------------------------------------------------
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole()))
                .toList();
    }

    // -------------------------------------------------------
    // DELETE USER  — ADMIN only (enforced in SecurityConfig)
    // -------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (userService.getUserById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
