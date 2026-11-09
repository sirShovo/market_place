package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.ProductRestMapper;
import application.adapters.in.rest.requests.ChangeStatusRequest;
import application.adapters.in.rest.requests.PublishProductRequest;
import application.adapters.in.rest.requests.UpdateProductRequest;
import application.adapters.in.rest.responses.ProductResponse;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.in.ChangeProductStatusUseCase;
import application.domain.ports.in.ConsultCatalogUseCase;
import application.domain.ports.in.PublishProductUseCase;
import application.domain.ports.in.UpdateProductUseCase;
import application.infrastructure.config.ApiResponse;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final PublishProductUseCase publishProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ChangeProductStatusUseCase changeProductStatusUseCase;
    private final ConsultCatalogUseCase consultCatalogUseCase;
    private final ProductRestMapper productRestMapper;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @GetMapping("/catalog")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> consultCatalog() {
        List<ProductResponse> catalog = consultCatalogUseCase.consultPublished()
                .stream()
                .map(productRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Published catalog retrieved successfully", catalog));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> publish(
            @Valid @RequestBody PublishProductRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        Product published = publishProductUseCase.publish(
                requester, productRestMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product published successfully", productRestMapper.toResponse(published)));
    }

    @PutMapping("/{identifier}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable String identifier,
            @Valid @RequestBody UpdateProductRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        Product updated = updateProductUseCase.update(
                requester, productRestMapper.toDomain(identifier, request));
        return ResponseEntity.ok(
                ApiResponse.ok("Product updated successfully", productRestMapper.toResponse(updated)));
    }

    @PatchMapping("/{identifier}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> changeStatus(
            @PathVariable String identifier,
            @Valid @RequestBody ChangeStatusRequest request) {
        User requester = authenticatedUserResolver.resolveUser();
        Product updated = changeProductStatusUseCase.changeStatus(
                requester, productRestMapper.toStatusTarget(identifier, request.status()));
        return ResponseEntity.ok(
                ApiResponse.ok("Product status updated successfully", productRestMapper.toResponse(updated)));
    }
}
