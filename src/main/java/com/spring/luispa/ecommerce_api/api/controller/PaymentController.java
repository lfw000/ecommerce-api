package com.spring.luispa.ecommerce_api.api.controller;

import com.spring.luispa.ecommerce_api.api.dto.request.ProcessPaymentRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RefundRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.PaymentResponse;
import com.spring.luispa.ecommerce_api.security.CurrentUser;
import com.spring.luispa.ecommerce_api.security.UserDetailsImpl;
import com.spring.luispa.ecommerce_api.services.PaymentService;
import com.spring.luispa.ecommerce_api.shared.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment and refund management")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Order payment", description = "Retrieves the payment associated with a user's order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "403", description = "The order does not belong to the user")
    })
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@CurrentUser UserDetailsImpl currentUser,
                                                               @Parameter(description = "Order ID", example = "1")
                                                               @PathVariable Long orderId) {
        PaymentResponse payment = paymentService.findByOrderIdForUser(orderId, currentUser.getId());

        return ResponseEntity.status(HttpStatus.OK).body(payment);
    }

    @PostMapping("/order/{orderId}/process")
    @Operation(summary = "Process payment", description = "Process payment for a pending order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "The order is not pending or has already been paid"),
            @ApiResponse(responseCode = "403", description = "The order does not belong to the user")
    })
    public ResponseEntity<PaymentResponse> processPayment(@CurrentUser UserDetailsImpl currentUser,
                                                          @Parameter(description = "Order ID", example = "1")
                                                          @PathVariable Long orderId,
                                                          @Valid @RequestBody ProcessPaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(orderId, request, currentUser.getId() ));
    }

    // Administrator endpoints

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payment (admin)", description = "Returns any payment by ID. Requires the ADMIN role.")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @Parameter(description = "Payment ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findByIdForAdmin(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all payments (admin)", description = "Retrieves all payments from the system. Requires the ADMIN role.")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.findAllForAdmin());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Payments by status (admin)", description = "Filters payments by status. Requires the ADMIN role.")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @Parameter(description = "Payment status", example = "COMPLETED")
            @PathVariable String status) {
        return ResponseEntity.ok(paymentService.findByStatus(PaymentStatus.PROCESSING.valueOf(status)));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Full refund (admin)", description = "Processes a full refund of the payment. Requires the ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund processed"),
            @ApiResponse(responseCode = "400", description = "The payment is not-refundable"),
            @ApiResponse(responseCode = "403", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> refundPayment(
            @CurrentUser UserDetailsImpl currentUser,
            @Parameter(description = "Payment ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {

        String userRole = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "ADMIN" : "USER";

        PaymentResponse response = paymentService.refundPayment(id, request, currentUser.getId(), userRole);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{id}/partial-refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Partial refund (admin)", description = "Processes a partial refund of the payment. Requires the ADMIN role.")
    public ResponseEntity<PaymentResponse> partialRefundPayment(
            @Parameter(description = "Payment ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.partialRefundPayment(id, request));
    }

    //@GetMapping("/user")
    //public ResponseEntity<List<PaymentResponse>> getUserPayments(@CurrentUser UserDetailsImpl currentUser) {
    //    return ResponseEntity.status(HttpStatus.OK).body(paymentService.findPaymentsByUserId(currentUser.getId()));
    //}
}
