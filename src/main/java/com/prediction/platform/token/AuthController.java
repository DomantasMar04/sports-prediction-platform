package com.prediction.platform.token;

import com.prediction.platform.dto.AuthorizationResponse;
import com.prediction.platform.dto.LoginRequest;
import com.prediction.platform.dto.RegisterRequest;
import com.prediction.platform.dto.UserResponse;
import com.prediction.platform.dto.ProfileResponse;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthorizationResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthorizationResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthorizationResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthorizationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        UserResponse response = authService.getCurrentUser(email);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> profile(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        ProfileResponse response = authService.getProfile(email);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        authService.deleteCurrentUser(email);
        return ResponseEntity.noContent().build();
    }
}