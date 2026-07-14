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
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.InsufficientStockException;
import com.spring.luispa.ecommerce_api.shared.exception.ProductNotActiveException;
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
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;
    private final LoggingAspect loggingAspect;

    public CartService(CartRepository cartRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository,
                       CartMapper cartMapper, LoggingAspect loggingAspect) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartMapper = cartMapper;
        this.loggingAspect = loggingAspect;
    }

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
        log.info("Adding to cart: userId={}, productId={}, quantity={}",
                userId, request.getProductId(), request.getQuantity());
        loggingAspect.setUserIdInMDC(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.warn("Product not found for cart: productId={}", request.getProductId());
                    return new ResourceNotFoundException("Product not found with id: " + request.getProductId());
                });

        if (!product.getActive()) {
            log.warn("Product not active: productId={}, sku={}", product.getId(), product.getSku());
            throw new ProductNotActiveException(product.getId(), product.getSku());
        }

        if (request.getQuantity() > product.getStock()) {
            log.warn("Insufficient stock: productId={}, sku={}, requested={}, available={}",
                    product.getId(), product.getSku(), request.getQuantity(), product.getStock());
            throw new InsufficientStockException(
                    product.getId(),
                    product.getSku(),
                    request.getQuantity(),
                    product.getStock());
        }

        Cart cart = getOrCreateActiveCartWithItems(userId);
        cart.addItem(product, request.getQuantity());

        Cart savedCart = cartRepository.save(cart);

        log.info("Added to cart: userId={}, productId={}, quantity={}, newTotalItems={}",
                userId, request.getProductId(), request.getQuantity(), savedCart.getTotalItems());

        return cartMapper.toResponse(savedCart);
    }

    private Cart getOrCreateActiveCart(Long userId) {
        return cartRepository.findActiveCartByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

                    return new Cart(user);
                });
    }

    private Cart getOrCreateActiveCartWithItems(Long userId) {
        return cartRepository.findActiveCartWithItems(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

                    return new Cart(user);
                });
    }

    @Transactional
    public CartResponse updateCartItem(Long userId, UpdateCartItemRequest request) {
        log.info("Updating cart item: userId={}, productId={}, newQuantity={}",
                userId, request.getProductId(), request.getQuantity());
        loggingAspect.setUserIdInMDC(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.warn("Product not found for cart update: productId={}",  request.getProductId());
                    return new ResourceNotFoundException("Product not found with id: " + request.getProductId());
                });

        if (!product.getActive()) {
            log.warn("Product not active: productId={}, sku={}", product.getId(), product.getSku());
            throw new ProductNotActiveException(product.getId(), product.getSku());
        }

        if (request.getQuantity() > product.getStock()) {
            log.warn("Insufficient stock: productId={}, sku={}, requested={}, avaiable={}",
                    product.getId(), product.getSku(), request.getQuantity(), product.getStock());
            throw new InsufficientStockException(
                    product.getId(),
                    product.getSku(),
                    product.getStock(),
                    request.getQuantity());
        }

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

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found for cart removal: productId={}", productId);
                    return new ResourceNotFoundException("Product not found with id: " + productId);
                });

        Cart cart = cartRepository.findActiveCartWithItems(userId)
                .orElseThrow(() -> {
                    log.warn("No active cart found for removal: userId={}", userId);
                    return new BusinessRuleException("No active cart found for user");
                });

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

    public boolean hasItems(Long userId) {
        Cart cart = cartRepository.findActiveCartByUserId(userId).orElse(null);

        return cart != null && cart.getTotalItems() > 0;
    }

    public int getTotalItems(Long userId) {
        Cart cart = cartRepository.findActiveCartByUserId(userId).orElse(null);

        return cart !=  null ? cart.getTotalItems() : 0;
    }

    public java.math.BigDecimal getTotalAmount(Long userId) {
        Cart cart = cartRepository.findActiveCartByUserId(userId).orElse(null);

        return cart != null ? cart.getTotalAmount() : java.math.BigDecimal.ZERO;
    }
}
