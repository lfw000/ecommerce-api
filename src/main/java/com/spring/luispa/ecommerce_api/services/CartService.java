package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.api.dto.request.AddToCartRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateCartItemRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.CartResponse;
import com.spring.luispa.ecommerce_api.domain.cart.Cart;
import com.spring.luispa.ecommerce_api.domain.cart.CartRepository;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.product.ProductRepository;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.domain.user.UserRepository;
import com.spring.luispa.ecommerce_api.mappers.CartMapper;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository, CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartMapper = cartMapper;
    }

    // Helper methods

    public CartResponse getActiveCart(Long userId) {
        Cart cart = getOrCreateActiveCart(userId);

        return cartMapper.toResponse(cart);
    }

    public CartResponse getActiveCartWithItems(Long userId) {
        Cart cart = getOrCreateActiveCartWithItems(userId);

        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        if (!product.getActive()) {
            throw new BusinessException("Product is not available: " + product.getName());
        }

        if (request.getQuantity() > product.getStock()) {
            throw new BusinessException("Insufficient stock. Available: " +  product.getStock());
        }

        Cart cart = getOrCreateActiveCartWithItems(userId);
        cart.addItem(product, request.getQuantity());

        Cart savedCart = cartRepository.save(cart);

        CartResponse cartResponse = cartMapper.toResponse(savedCart);

        //return cartMapper.toResponse(savedCart);

        System.out.println(cartResponse);

        return cartResponse;
    }

    private Cart getOrCreateActiveCart(Long userId) {
        return cartRepository.findByUserIdAndActiveTrue(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

                    return new Cart(user);

                });
    }

    private Cart getOrCreateActiveCartWithItems(Long userId) {
        return cartRepository.findActiveCartWithItems(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

                    return new Cart(user);
                });
    }

    @Transactional
    public CartResponse updateCartItem(Long userId, UpdateCartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (!product.getActive()) {
            throw new BusinessException("Product is not available: " + product.getName());
        }

        if (request.getQuantity() > product.getStock()) {
            throw new BusinessException("Insufficient stock. Available: " +  product.getStock());
        }

        Cart cart = cartRepository.findActiveCartWithItems(userId)
                .orElseThrow(() -> new BusinessException("No active cart found for user"));

        cart.updateItemQuantity(product, request.getQuantity());

        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Cart cart = cartRepository.findActiveCartWithItems(userId)
                .orElseThrow(() -> new BusinessException("No active cart found for user"));

        cart.removeItem(product);

        return cartMapper.toResponse(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findActiveCartWithItems(userId)
                .orElseThrow(() -> new BusinessException("No active cart found for user"));

        cart.clear();
    }

    public boolean hasItems(Long userId) {
        Cart cart = cartRepository.findByUserIdAndActiveTrue(userId).orElse(null);

        return cart != null && cart.getTotalItems() > 0;
    }

    public int getTotalItems(Long userId) {
        Cart cart = cartRepository.findByUserIdAndActiveTrue(userId).orElse(null);

        return cart !=  null ? cart.getTotalItems() : 0;
    }

    public java.math.BigDecimal getTotalAmount(Long userId) {
        Cart cart = cartRepository.findByUserIdAndActiveTrue(userId).orElse(null);

        return cart != null ? cart.getTotalAmount() : java.math.BigDecimal.ZERO;
    }
}
