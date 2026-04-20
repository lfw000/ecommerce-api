package com.spring.luispa.ecommerce_api.api.controller;


import com.spring.luispa.ecommerce_api.api.dto.request.AddToCartRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateCartItemRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.CartResponse;
import com.spring.luispa.ecommerce_api.security.CurrentUser;
import com.spring.luispa.ecommerce_api.security.UserDetailsImpl;
import com.spring.luispa.ecommerce_api.services.CartService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "User shopping cart management")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "View cart", description = "Retrieves the active cart for the authenticated user, including all items.")
    @ApiResponse(responseCode = "200", description = "Cart retrieved successfully",
        content = @Content(schema = @Schema(implementation = CartResponse.class)))
    public ResponseEntity<CartResponse> getCart(@CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.getActiveCartWithItems(currentUser.getId()));
    }

    @GetMapping("/count")
    @Operation(summary = "Count items", description = "Returns the total number of items in the cart")
    public ResponseEntity<Integer> getCartItemCount(@CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.getTotalItems(currentUser.getId()));
    }

    @GetMapping("/total")
    @Operation(summary = "Cart total", description = "Returns the total amount of the cart")
    public ResponseEntity<java.math.BigDecimal> getCartTotal(@CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.getTotalAmount(currentUser.getId()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add to cart", description = "Adds a product to the cart. If it already exists, increases the quantity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product successfully added"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock or product unavailable"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<CartResponse> addToCart(@CurrentUser UserDetailsImpl currentUser,
                                                  @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.addToCart(currentUser.getId(), request));
    }

    @PutMapping("/items")
    @Operation(summary = "Update quantity", description = "Update the quantity of a product in the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantity updated"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Product not found in the cart")
    })
    public ResponseEntity<CartResponse> updateCartItem(@CurrentUser UserDetailsImpl currentUser,
                                                       @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.updateCartItem(currentUser.getId(), request));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove from cart", description = "Completely removes product from the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product removed"),
            @ApiResponse(responseCode = "404", description = "Product not found in the cart")
    })
    public ResponseEntity<CartResponse> removeFromCart(@CurrentUser UserDetailsImpl currentUser,
                                                       @Parameter(description = "ID of the product to remove", example = "1")
                                                       @PathVariable Long productId) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.removeFromCart(currentUser.getId(), productId));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all products from the cart")
    @ApiResponse(responseCode = "200", description = "Cart successfully cleared")
    public ResponseEntity<Void> clearCart(@CurrentUser UserDetailsImpl currentUser) {
        cartService.clearCart(currentUser.getId());

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}