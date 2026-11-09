package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.InventoryRestMapper;
import application.adapters.in.rest.mappers.ProductRestMapper;
import application.adapters.in.rest.requests.InventoryOperationRequest;
import application.adapters.in.rest.responses.InventoryItemResponse;
import application.adapters.in.rest.responses.InventoryMovementResponse;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.User;
import application.domain.ports.in.AdjustInventoryUseCase;
import application.domain.ports.in.ConsultInventoryUseCase;
import application.domain.ports.in.RegisterInventoryEntryUseCase;
import application.domain.ports.in.RegisterInventoryReturnUseCase;
import application.domain.ports.in.ReleaseReservationUseCase;
import application.domain.ports.in.ReserveInventoryUseCase;
import application.infrastructure.config.ApiResponse;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final RegisterInventoryEntryUseCase registerInventoryEntryUseCase;
    private final ReserveInventoryUseCase reserveInventoryUseCase;
    private final ReleaseReservationUseCase releaseReservationUseCase;
    private final AdjustInventoryUseCase adjustInventoryUseCase;
    private final RegisterInventoryReturnUseCase registerInventoryReturnUseCase;
    private final ConsultInventoryUseCase consultInventoryUseCase;
    private final InventoryRestMapper inventoryRestMapper;
    private final ProductRestMapper productRestMapper;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping("/entries")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> registerEntry(
            @Valid @RequestBody InventoryOperationRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        InventoryItem updated = registerInventoryEntryUseCase.registerEntry(
                requester, inventoryRestMapper.toProbe(request), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Inventory entry registered", inventoryRestMapper.toItemResponse(updated)));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ApiResponse<InventoryMovementResponse>> reserve(
            @Valid @RequestBody InventoryOperationRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        InventoryMovement movement = reserveInventoryUseCase.reserve(
                requester, inventoryRestMapper.toProbe(request), request.quantity());
        return ResponseEntity.ok(
                ApiResponse.ok("Inventory reserved", inventoryRestMapper.toMovementResponse(movement)));
    }

    @PostMapping("/releases")
    public ResponseEntity<ApiResponse<InventoryMovementResponse>> release(
            @Valid @RequestBody InventoryOperationRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        InventoryMovement movement = releaseReservationUseCase.release(
                requester, inventoryRestMapper.toProbe(request), request.quantity());
        return ResponseEntity.ok(
                ApiResponse.ok("Inventory reservation released", inventoryRestMapper.toMovementResponse(movement)));
    }

    @PostMapping("/adjustments")
    public ResponseEntity<ApiResponse<InventoryMovementResponse>> adjust(
            @Valid @RequestBody InventoryOperationRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        InventoryMovement movement = adjustInventoryUseCase.adjust(
                requester, inventoryRestMapper.toProbe(request), request.quantity());
        return ResponseEntity.ok(
                ApiResponse.ok("Inventory adjusted", inventoryRestMapper.toMovementResponse(movement)));
    }

    @PostMapping("/returns")
    public ResponseEntity<ApiResponse<InventoryMovementResponse>> registerReturn(
            @Valid @RequestBody InventoryOperationRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        InventoryMovement movement = registerInventoryReturnUseCase.registerReturn(
                requester, inventoryRestMapper.toProbe(request), request.quantity());
        return ResponseEntity.ok(
                ApiResponse.ok("Inventory return registered", inventoryRestMapper.toMovementResponse(movement)));
    }

    @GetMapping("/product/{productIdentifier}")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> consultByProduct(
            @PathVariable String productIdentifier) {
        User requester = authenticatedUserResolver.resolveUser();
        List<InventoryItemResponse> items = consultInventoryUseCase
                .consult(requester, productRestMapper.toProbe(productIdentifier))
                .stream()
                .map(inventoryRestMapper::toItemResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Inventory retrieved successfully", items));
    }
}
