package com.library.management.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Random UUID token sent to the user */
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    /** The email this token belongs to */
    @Column(nullable = false)
    private String email;

    /** Token expires after 1 hour */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean used;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, String email, LocalDateTime expiresAt) {
        this.token     = token;
        this.email     = email;
        this.expiresAt = expiresAt;
        this.used      = false;
    }

    public Long          getId()       { return id; }
    public String        getToken()    { return token; }
    public String        getEmail()    { return email; }
    public LocalDateTime getExpiresAt(){ return expiresAt; }
    public boolean       isUsed()      { return used; }
    public void          setUsed(boolean used) { this.used = used; }
}
