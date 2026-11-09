package application.adapters.in.rest.controllers;

import application.adapters.in.rest.requests.LoginRequest;
import application.adapters.in.rest.responses.AuthResponse;
import application.domain.ports.in.LoginUseCase;
import application.infrastructure.config.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        String token = loginUseCase.login(request.username(), request.password());
        AuthResponse response = new AuthResponse(token, request.username());
        return ResponseEntity.ok(ApiResponse.ok("Authentication successful", response));
    }
}
