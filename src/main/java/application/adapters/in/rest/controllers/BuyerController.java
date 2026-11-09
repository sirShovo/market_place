package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.BuyerRestMapper;
import application.adapters.in.rest.requests.ChangeStatusRequest;
import application.adapters.in.rest.requests.RegisterBuyerRequest;
import application.adapters.in.rest.requests.UpdateBuyerRequest;
import application.adapters.in.rest.responses.BuyerResponse;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.in.ChangeBuyerCommercialStatusUseCase;
import application.domain.ports.in.ConsultBuyerUseCase;
import application.domain.ports.in.RegisterBuyerUseCase;
import application.domain.ports.in.UpdateBuyerUseCase;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final RegisterBuyerUseCase registerBuyerUseCase;
    private final UpdateBuyerUseCase updateBuyerUseCase;
    private final ChangeBuyerCommercialStatusUseCase changeBuyerCommercialStatusUseCase;
    private final ConsultBuyerUseCase consultBuyerUseCase;
    private final BuyerRestMapper buyerRestMapper;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<BuyerResponse>> register(
            @Valid @RequestBody RegisterBuyerRequest request) {
        Buyer created = registerBuyerUseCase.register(
                buyerRestMapper.toDomain(request),
                buyerRestMapper.toAccountDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Buyer registered successfully", buyerRestMapper.toResponse(created)));
    }

    @PutMapping("/{identification}")
    public ResponseEntity<ApiResponse<BuyerResponse>> update(
            @PathVariable String identification,
            @Valid @RequestBody UpdateBuyerRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        Buyer updated = updateBuyerUseCase.update(
                requester, buyerRestMapper.toDomain(identification, request));
        return ResponseEntity.ok(
                ApiResponse.ok("Buyer updated successfully", buyerRestMapper.toResponse(updated)));
    }

    @PatchMapping("/{identification}/status")
    public ResponseEntity<ApiResponse<BuyerResponse>> changeStatus(
            @PathVariable String identification,
            @Valid @RequestBody ChangeStatusRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        Buyer updated = changeBuyerCommercialStatusUseCase.changeCommercialStatus(
                requester, buyerRestMapper.toStatusTarget(identification, request.status()));
        return ResponseEntity.ok(
                ApiResponse.ok("Buyer status updated successfully", buyerRestMapper.toResponse(updated)));
    }

    @GetMapping("/{identification}")
    public ResponseEntity<ApiResponse<BuyerResponse>> consult(@PathVariable String identification) {
        User requester = authenticatedUserResolver.resolveUser();
        Buyer found = consultBuyerUseCase.consult(requester, buyerRestMapper.toProbe(identification));
        return ResponseEntity.ok(
                ApiResponse.ok("Buyer retrieved successfully", buyerRestMapper.toResponse(found)));
    }
}
