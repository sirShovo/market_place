package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.UserRestMapper;
import application.adapters.in.rest.requests.ChangeStatusRequest;
import application.adapters.in.rest.requests.RegisterUserRequest;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.models.User;
import application.domain.ports.in.ChangeUserStatusUseCase;
import application.domain.ports.in.ConsultUserUseCase;
import application.domain.ports.in.RegisterUserUseCase;
import application.infrastructure.config.ApiResponse;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final ChangeUserStatusUseCase changeUserStatusUseCase;
    private final ConsultUserUseCase consultUserUseCase;
    private final UserRestMapper userRestMapper;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterUserRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        User created = registerUserUseCase.register(requester, userRestMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", userRestMapper.toResponse(created)));
    }

    @PatchMapping("/{username}/status")
    public ResponseEntity<ApiResponse<UserResponse>> changeStatus(
            @PathVariable String username,
            @Valid @RequestBody ChangeStatusRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        User updated = changeUserStatusUseCase.changeStatus(
                requester, userRestMapper.toStatusTarget(username, request.status()));
        return ResponseEntity.ok(
                ApiResponse.ok("User status updated successfully", userRestMapper.toResponse(updated)));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> consult(@PathVariable String username) {
        User requester = authenticatedUserResolver.resolveUser();
        User found = consultUserUseCase.consult(requester, userRestMapper.toProbe(username));
        return ResponseEntity.ok(
                ApiResponse.ok("User retrieved successfully", userRestMapper.toResponse(found)));
    }
}
