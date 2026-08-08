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
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
import com.spring.luispa.ecommerce_api.mappers.CartMapper;
import com.spring.luispa.ecommerce_api.services.validation.CartValidator;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final CartValidator cartValidator;
    private final LoggingAspect loggingAspect;


    public CartService(CartRepository cartRepository,
                       CartMapper cartMapper,
                       CartValidator cartValidator,
                       LoggingAspect loggingAspect) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.cartValidator = cartValidator;
        this.loggingAspect = loggingAspect;
    }

    private Cart getOrCreateActiveCart(Long userId) {
        return cartRepository.findActiveCartByUserId(userId)
                .orElseGet(() -> {
                    User user = cartValidator.validateUser(userId);

                    Cart newCart = new Cart(user);

                    return cartRepository.save(newCart);
                });
    }

    private Cart getOrCreateActiveCartWithItems(Long userId) {
        return cartRepository.findActiveCartWithItems(userId)
                .orElseGet(() -> {
                    User user = cartValidator.validateUser(userId);
                    Cart newCart = new Cart(user);
                    return cartRepository.save(newCart);
                });
    }

    public Cart getActiveCartForCheckout(Long userId) {
        return cartRepository.findCartForCheckout(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active cart found for user"));
    }

    public CartResponse getActiveCart(Long userId) {
        Cart cart = getOrCreateActiveCart(userId);

        return cartMapper.toResponse(cart);
    }

    public CartResponse getActiveCartWithItems(Long userId) {
        Cart cart = getOrCreateActiveCartWithItems(userId);

        return cartMapper.toResponse(cart);
    }

    public int getTotalItems(Long userId) {
        Cart cart = cartRepository.findActiveCartByUserId(userId).orElse(null);

        return cart !=  null ? cart.getTotalItems() : 0;
    }

    public java.math.BigDecimal getTotalAmount(Long userId) {
        Cart cart = cartRepository.findActiveCartByUserId(userId).orElse(null);

        return cart != null ? cart.getTotalAmount() : java.math.BigDecimal.ZERO;
    }

    public boolean hasItems(Long userId) {
        Cart cart = cartRepository.findActiveCartByUserId(userId).orElse(null);

        return cart != null && cart.getTotalItems() > 0;
    }

    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        log.info("Adding to cart: userId={}, productId={}, quantity={}",
                userId, request.getProductId(), request.getQuantity());
        loggingAspect.setUserIdInMDC(userId);

        cartValidator.validateQuantity(request.getQuantity());
        User user = cartValidator.validateUser(userId);
        Product product = cartValidator.validateProduct(request.getProductId());
        cartValidator.validateStock(product, request.getQuantity());

        Cart cart = getOrCreateActiveCartWithItems(userId);
        cart.addItem(product, request.getQuantity());

        Cart savedCart = cartRepository.save(cart);

        log.info("Added to cart: userId={}, productId={}, quantity={}, newTotalItems={}",
                userId, request.getProductId(), request.getQuantity(), savedCart.getTotalItems());

        return cartMapper.toResponse(savedCart);
    }

    @Transactional
    public CartResponse updateCartItem(Long userId, UpdateCartItemRequest request) {
        log.info("Updating cart item: userId={}, productId={}, newQuantity={}",
                userId, request.getProductId(), request.getQuantity());
        loggingAspect.setUserIdInMDC(userId);

        cartValidator.validateQuantity(request.getQuantity());
        Product product = cartValidator.validateProduct(request.getProductId());
        cartValidator.validateStock(product, request.getQuantity());

        Cart cart = cartRepository.findActiveCartWithItems(userId)
                .orElseThrow(() -> {
                    log.warn("No active cart found for user: {}", userId);
                    return new BusinessRuleException("No active cart found for user");
                });

        cart.updateItemQuantity(product, request.getQuantity());

        log.info("Cart item updated: userId={}, productId={}, newQuantity={}, totalItems={}",
                userId, request.getProductId(), request.getQuantity(), cart.getTotalItems());

        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(Long userId, Long productId) {
        log.info("Removing from cart: userId={}, productId={}", userId, productId);
        loggingAspect.setUserIdInMDC(userId);

        Product product = cartValidator.validateProduct(productId);

        Cart cart = cartRepository.findActiveCartWithItems(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("No active cart found for user"));

        cart.removeItem(product);

        log.info("Removed from cart: userId={}, productId={}, remainingItems={}",
                userId, productId, cart.getTotalItems());

        return cartMapper.toResponse(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart: userId={}", userId);
        loggingAspect.setUserIdInMDC(userId);

        Cart cart = cartRepository.findActiveCartWithItems(userId)
                .orElseThrow(() -> {
                    log.warn("No active cart found to clear: userId={}", userId);
                    return new BusinessRuleException("No active cart found for user");
                });

        int itemsCleared = cart.getTotalItems();
        cart.clear();

        log.info("Cart cleared: userId={}, itemsRemoved={}", userId, itemsCleared);
    }

}
