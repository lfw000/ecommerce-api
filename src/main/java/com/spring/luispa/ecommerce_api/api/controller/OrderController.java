package com.spring.luispa.ecommerce_api.api.controller;

import com.spring.luispa.ecommerce_api.api.dto.request.CancelOrderRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.CreateOrderRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.OrderResponse;
import com.spring.luispa.ecommerce_api.security.CurrentUser;
import com.spring.luispa.ecommerce_api.security.UserDetailsImpl;
import com.spring.luispa.ecommerce_api.services.OrderService;
import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Purchase order management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create order", description = "Creates an order from the user's active shopping cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order successfully created",
                content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Empty cart or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<OrderResponse> createOrder(@CurrentUser UserDetailsImpl currentUser,
                                                     @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED ).body(orderService.createOrderFromCart(currentUser.getId(), request));
    }

    @GetMapping
    @Operation(summary = "List user orders", description = "Retrieves all orders for the authenticated user")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findByUserId(currentUser.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Returns the details of a specific user order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "The order does not belong to the user")
    })
    public ResponseEntity<OrderResponse> getOrderById(@CurrentUser UserDetailsImpl currentUser,
                                                      @Parameter(description = "Order ID", example = "1")
                                                      @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findByIdForUser(id,  currentUser.getId()));
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "Full order details", description = "Retrieves the order with items, user and payment")
    public ResponseEntity<OrderResponse> getOrderDetail(@CurrentUser UserDetailsImpl currentUser,
                                                        @Parameter(description = "Order ID", example = "1")
                                                        @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findOrderDetailByIdForUser(id, currentUser.getId()));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Orders by status", description = "Filters the user's orders by status")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@CurrentUser UserDetailsImpl currentUser,
                                                                 @Parameter(description = "Order status", example = "PAID")
                                                                 @PathVariable OrderStatus status) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findByUserIdAndStatus(currentUser.getId(), status));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels a pending or paid order for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order canceled"),
            @ApiResponse(responseCode = "400", description = "Order cannot be canceled"),
            @ApiResponse(responseCode = "403", description = "Order does not belong to the user")
    })
    public ResponseEntity<OrderResponse> cancelOrder(@CurrentUser UserDetailsImpl currentUser,
                                                     @Parameter(description = "Order ID", example = "1")
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody CancelOrderRequest request) {


        String userRole = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "ADMIN" : "USER";

        OrderResponse response = orderService.cancelOrder(id, request, currentUser.getId(), userRole);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Administrator methods

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all orders (admin)", description = "Retrieves all orders from the system. Requires ADMIN role.")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findRecentOrdersWithItems(null, 100));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get order (admin)", description = "Returns any order by ID. Requires the ADMIN role.")
    public ResponseEntity<OrderResponse> getOrderForAdmin(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findByIdForAdmin(id));
    }

    @PutMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark as shipped (admin)", description = "Updates the order status to SHIPPED. Requires the ADMIN role.")
    public ResponseEntity<OrderResponse> shipOrder(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Tracking number", example = "1Z999AA10123456784")
            @RequestParam String trackingNumber) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.shipOrder(id, trackingNumber));
    }

    @PutMapping("/{id}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark as delivered (admin)", description = "Updates the order status to DELIVERED. Requires the ADMIN role.")
    public ResponseEntity<OrderResponse> deliverOrder(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.deliverOrder(id));
    }
}
