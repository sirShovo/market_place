package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.AuditRestMapper;
import application.adapters.in.rest.responses.AuditLogResponse;
import application.domain.models.AuditableEntity;
import application.domain.models.User;
import application.domain.ports.in.ConsultAuditLogUseCase;
import application.infrastructure.config.ApiResponse;
import application.infrastructure.security.AuthenticatedUserResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final ConsultAuditLogUseCase consultAuditLogUseCase;
    private final AuditRestMapper auditRestMapper;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> consult(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        User requester = authenticatedUserResolver.resolveUser();
        List<AuditLogResponse> logs = consultAuditLogUseCase
                .consult(requester, AuditableEntity.of(entityType, entityId))
                .stream()
                .map(auditRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Audit history retrieved successfully", logs));
    }
}
