package com.library.management.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex) {

        return error(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password."
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex) {

        return error(
                HttpStatus.FORBIDDEN,
                "Access denied."
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex) {

        return error(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        String msg;

        if (ex.getMessage() != null
                && ex.getMessage().contains("isbn")) {

            msg = "A book with that ISBN already exists.";

        } else if (ex.getMessage() != null
                && ex.getMessage().contains("email")) {

            msg = "That email is already registered.";

        } else {

            msg = "A database constraint was violated.";
        }

        return error(HttpStatus.CONFLICT, msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {

        // IMPORTANT:
        // Print the real exception in the Spring Boot console.
        ex.printStackTrace();

        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().getName() + ": " + ex.getMessage()
        );
    }

    private ResponseEntity<Map<String, Object>> error(
            HttpStatus status,
            String message) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity
                .status(status)
                .body(body);
    }
}