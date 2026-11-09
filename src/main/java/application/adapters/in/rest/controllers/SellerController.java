package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.ProductRestMapper;
import application.adapters.in.rest.mappers.SellerRestMapper;
import application.adapters.in.rest.requests.OnboardSellerRequest;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.SellerResponse;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.in.ConsultSellerProductsUseCase;
import application.domain.ports.in.ConsultSellerUseCase;
import application.domain.ports.in.OnboardSellerUseCase;
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
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final OnboardSellerUseCase onboardSellerUseCase;
    private final ConsultSellerUseCase consultSellerUseCase;
    private final ConsultSellerProductsUseCase consultSellerProductsUseCase;
    private final SellerRestMapper sellerRestMapper;
    private final ProductRestMapper productRestMapper;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping("/onboard")
    public ResponseEntity<ApiResponse<SellerResponse>> onboard(
            @Valid @RequestBody OnboardSellerRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        Seller onboarded = onboardSellerUseCase.onboard(
                requester,
                sellerRestMapper.toSellerDomain(request),
                sellerRestMapper.toFirstWarehouseDomain(request),
                sellerRestMapper.toAccountDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Seller onboarded successfully", sellerRestMapper.toResponse(onboarded)));
    }

    @GetMapping("/{identification}")
    public ResponseEntity<ApiResponse<SellerResponse>> consult(@PathVariable String identification) {
        User requester = authenticatedUserResolver.resolveUser();
        Seller found = consultSellerUseCase.consult(
                requester, sellerRestMapper.toProbe(identification));
        return ResponseEntity.ok(
                ApiResponse.ok("Seller retrieved successfully", sellerRestMapper.toResponse(found)));
    }

    @GetMapping("/{identification}/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> consultProducts(
            @PathVariable String identification) {
        User requester = authenticatedUserResolver.resolveUser();
        List<ProductResponse> products = consultSellerProductsUseCase
                .consultBySeller(requester, sellerRestMapper.toProbe(identification))
                .stream()
                .map(productRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Seller products retrieved successfully", products));
    }
}
