package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.BuyerRestMapper;
import application.adapters.in.rest.mappers.OrderRestMapper;
import application.adapters.in.rest.responses.OrderResponse;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.in.CheckoutCartUseCase;
import application.domain.ports.in.ConfirmDeliveryUseCase;
import application.domain.ports.in.ConsultBuyerOrdersUseCase;
import application.domain.ports.in.ConsultOrderUseCase;
import application.domain.ports.in.DispatchOrderUseCase;
import application.domain.ports.in.ProcessOrderPaymentUseCase;
import application.infrastructure.config.ApiResponse;
import application.infrastructure.security.AuthenticatedUserResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CheckoutCartUseCase checkoutCartUseCase;
    private final ProcessOrderPaymentUseCase processOrderPaymentUseCase;
    private final DispatchOrderUseCase dispatchOrderUseCase;
    private final ConfirmDeliveryUseCase confirmDeliveryUseCase;
    private final ConsultOrderUseCase consultOrderUseCase;
    private final ConsultBuyerOrdersUseCase consultBuyerOrdersUseCase;
    private final OrderRestMapper orderRestMapper;
    private final BuyerRestMapper buyerRestMapper;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout() {
        Buyer buyer = authenticatedUserResolver.resolveBuyer();
        Order order = checkoutCartUseCase.checkout(buyer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order created at checkout", orderRestMapper.toResponse(order)));
    }

    @PostMapping("/{identifier}/pay")
    public ResponseEntity<ApiResponse<OrderResponse>> pay(@PathVariable String identifier) {
        Buyer buyer = authenticatedUserResolver.resolveBuyer();
        Order paid = processOrderPaymentUseCase.pay(buyer, orderRestMapper.toProbe(identifier));
        return ResponseEntity.ok(
                ApiResponse.ok("Order payment processed", orderRestMapper.toResponse(paid)));
    }

    @PostMapping("/{identifier}/dispatch")
    public ResponseEntity<ApiResponse<OrderResponse>> dispatch(@PathVariable String identifier) {
        User requester = authenticatedUserResolver.resolveUser();
        Order dispatched = dispatchOrderUseCase.dispatch(requester, orderRestMapper.toProbe(identifier));
        return ResponseEntity.ok(
                ApiResponse.ok("Order dispatched", orderRestMapper.toResponse(dispatched)));
    }

    @PostMapping("/{identifier}/deliver")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmDelivery(@PathVariable String identifier) {
        User requester = authenticatedUserResolver.resolveUser();
        Order delivered = confirmDeliveryUseCase.confirmDelivery(
                requester, orderRestMapper.toProbe(identifier));
        return ResponseEntity.ok(
                ApiResponse.ok("Order delivery confirmed", orderRestMapper.toResponse(delivered)));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ApiResponse<OrderResponse>> consult(@PathVariable String identifier) {
        User requester = authenticatedUserResolver.resolveUser();
        Order order = consultOrderUseCase.consult(requester, orderRestMapper.toProbe(identifier));
        return ResponseEntity.ok(
                ApiResponse.ok("Order retrieved successfully", orderRestMapper.toResponse(order)));
    }

    @GetMapping("/buyer/{buyerIdentification}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> consultByBuyer(
            @PathVariable String buyerIdentification) {
        User requester = authenticatedUserResolver.resolveUser();
        List<OrderResponse> orders = consultBuyerOrdersUseCase
                .consultByBuyer(requester, buyerRestMapper.toProbe(buyerIdentification))
                .stream()
                .map(orderRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(
                ApiResponse.ok("Buyer orders retrieved successfully", orders));
    }
}
