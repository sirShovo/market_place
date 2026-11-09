package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CartRestMapper;
import application.adapters.in.rest.requests.CartItemRequest;
import application.adapters.in.rest.requests.RemoveCartItemRequest;
import application.adapters.in.rest.responses.CartResponse;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.ports.in.AddCartItemUseCase;
import application.domain.ports.in.ClearCartUseCase;
import application.domain.ports.in.ConsultCartUseCase;
import application.domain.ports.in.RemoveCartItemUseCase;
import application.infrastructure.config.ApiResponse;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final AddCartItemUseCase addCartItemUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final ConsultCartUseCase consultCartUseCase;
    private final CartRestMapper cartRestMapper;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> consult() {
        Buyer buyer = authenticatedUserResolver.resolveBuyer();
        Cart cart = consultCartUseCase.consult(buyer);
        return ResponseEntity.ok(ApiResponse.ok("Cart retrieved successfully", cartRestMapper.toResponse(cart)));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody CartItemRequest request) {
        Buyer buyer = authenticatedUserResolver.resolveBuyer();
        Cart updated = addCartItemUseCase.addItem(buyer, cartRestMapper.toDomain(request));
        return ResponseEntity.ok(ApiResponse.ok("Item added to cart", cartRestMapper.toResponse(updated)));
    }

    @DeleteMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @Valid @RequestBody RemoveCartItemRequest request) {
        Buyer buyer = authenticatedUserResolver.resolveBuyer();
        Cart updated = removeCartItemUseCase.removeItem(buyer, cartRestMapper.toRemoveDomain(request));
        return ResponseEntity.ok(ApiResponse.ok("Item removed from cart", cartRestMapper.toResponse(updated)));
    }

    @DeleteMapping("/items/{productIdentifier}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItemByPath(
            @PathVariable String productIdentifier,
            @RequestParam(required = false) String variantAttributeName,
            @RequestParam(required = false) String variantValue) {
        Buyer buyer = authenticatedUserResolver.resolveBuyer();
        Cart updated = removeCartItemUseCase.removeItem(
                buyer,
                cartRestMapper.toRemoveDomain(productIdentifier, variantAttributeName, variantValue));
        return ResponseEntity.ok(ApiResponse.ok("Item removed from cart", cartRestMapper.toResponse(updated)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clear() {
        Buyer buyer = authenticatedUserResolver.resolveBuyer();
        Cart cleared = clearCartUseCase.clear(buyer);
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared", cartRestMapper.toResponse(cleared)));
    }
}
