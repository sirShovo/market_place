package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.WarehouseRestMapper;
import application.adapters.in.rest.requests.RegisterWarehouseRequest;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.ConsultWarehouseUseCase;
import application.domain.ports.in.RegisterWarehouseUseCase;
import application.infrastructure.config.ApiResponse;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final RegisterWarehouseUseCase registerWarehouseUseCase;
    private final ConsultWarehouseUseCase consultWarehouseUseCase;
    private final WarehouseRestMapper warehouseRestMapper;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseResponse>> register(
            @Valid @RequestBody RegisterWarehouseRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        Warehouse created = registerWarehouseUseCase.register(
                requester, warehouseRestMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Warehouse registered successfully", warehouseRestMapper.toResponse(created)));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> consult(@PathVariable String identifier) {
        User requester = authenticatedUserResolver.resolveUser();
        Warehouse found = consultWarehouseUseCase.consult(
                requester, warehouseRestMapper.toProbe(identifier));
        return ResponseEntity.ok(
                ApiResponse.ok("Warehouse retrieved successfully", warehouseRestMapper.toResponse(found)));
    }
}
