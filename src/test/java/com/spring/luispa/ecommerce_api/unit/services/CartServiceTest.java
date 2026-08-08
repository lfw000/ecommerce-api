package com.spring.luispa.ecommerce_api.unit.services;

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
import com.spring.luispa.ecommerce_api.services.CartService;
import com.spring.luispa.ecommerce_api.services.validation.CartValidator;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.ProductNotActiveException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import com.spring.luispa.ecommerce_api.test.helpers.CartTestHelper;
import com.spring.luispa.ecommerce_api.test.helpers.ProductTestHelper;
import com.spring.luispa.ecommerce_api.test.helpers.UserTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Service Tests")
public class CartServiceTest {

    // Mocks

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CartValidator cartValidator;

    @Mock
    private LoggingAspect loggingAspect;

    private CartService cartService;

    // Test data

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartResponse testResponse;
    private AddToCartRequest addRequest;
    private UpdateCartItemRequest updateRequest;

    @BeforeEach
    void setUp() {
        cartService = new CartService(
                cartRepository,
                cartMapper,
                cartValidator,
                loggingAspect);

        testUser = UserTestHelper.defaultUser(1L);
        testProduct = ProductTestHelper.defaultProduct(1L);
        testProduct.setStock(10);
        testCart = CartTestHelper.createCartWithProduct(testUser, testProduct, 2);
        testResponse = new CartResponse();
        testResponse.setId(1L);
        testResponse.setUserId(1L);

        addRequest = new AddToCartRequest();
        addRequest.setProductId(1L);
        addRequest.setQuantity(2);

        updateRequest = new UpdateCartItemRequest();
        updateRequest.setProductId(1L);
        updateRequest.setQuantity(3);
    }

    // Add-to-cart tests

    @Nested
    @DisplayName("Add to Cart Tests")
    class AddToCartTests {

        @Test
        @DisplayName("Should add product to cart when product exists and has stock")
        void shouldAddProductToCart_whenProductExists() {
            when(cartValidator.validateUser(1L)).thenReturn(testUser);
            when(cartValidator.validateProduct(1L)).thenReturn(testProduct);

            when(cartRepository.findActiveCartWithItems(1L)).thenReturn(Optional.of(testCart));
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
            when(cartMapper.toResponse(any(Cart.class))).thenReturn(testResponse);

            CartResponse result = cartService.addToCart(1L, addRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("Should create new cart if user has no active cart")
        void shouldCreateNewCart_whenNoActiveCart() {
            when(cartValidator.validateUser(any(Long.class))).thenReturn(testUser);
            when(cartValidator.validateProduct(any(Long.class))).thenReturn(testProduct);

            when(cartRepository.findActiveCartWithItems(1L)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
            when(cartMapper.toResponse(any(Cart.class))).thenReturn(testResponse);

            CartResponse result = cartService.addToCart(1L, addRequest);

            assertThat(result).isNotNull();
            verify(cartRepository, times(2)).save(any(Cart.class));
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowException_whenProductNotFound() {
            when(cartValidator.validateProduct(1L))
                    .thenThrow(new ResourceNotFoundException("Product not found"));

            assertThatThrownBy(() -> cartService.addToCart(1L, addRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when product is not active")
        void shouldThrowException_whenProductNotActive() {
            testProduct.setActive(false);
            when(cartValidator.validateProduct(1L))
                    .thenThrow(new ProductNotActiveException(testProduct.getId(), testProduct.getSku()));

            assertThatThrownBy(() -> cartService.addToCart(1L, addRequest))
                    .isInstanceOf(ProductNotActiveException.class);
        }

        @Test
        @DisplayName("Should throw exception when quantity exceeds stock")
        void shouldThrowException_whenQuantityExceedsStock() {
            addRequest.setQuantity(15);
            when(cartValidator.validateProduct(1L)).thenReturn(testProduct);
            doThrow(new BusinessRuleException("Insufficient stock"))
                    .when(cartValidator)
                    .validateStock(testProduct, 15);

            assertThatThrownBy(() -> cartService.addToCart(1L, addRequest))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("Should increment quantity when product already in cart")
        void shouldIncrementQuantity_whenProductAlreadyInCart() {
            when(cartValidator.validateUser(1L)).thenReturn(testUser);
            when(cartValidator.validateProduct(1L)).thenReturn(testProduct);

            when(cartRepository.findActiveCartWithItems(1L)).thenReturn(Optional.of(testCart));
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
            when(cartMapper.toResponse(any(Cart.class))).thenReturn(testResponse);

            cartService.addToCart(1L, addRequest);

            verify(cartRepository).save(any(Cart.class));
        }
    }

    // Shopping cart update tests

    @Nested
    @DisplayName("Update Cart Item Tests")
    class UpdateCartItemTests {

        @Test
        @DisplayName("Should update item quantity when product exists")
        void shouldUpdateItemQuantity_whenProductExists() {
            when(cartValidator.validateProduct(1L)).thenReturn(testProduct);

            when(cartRepository.findActiveCartWithItems(1L)).thenReturn(Optional.of(testCart));
            when(cartMapper.toResponse(any(Cart.class))).thenReturn(testResponse);

            CartResponse result = cartService.updateCartItem(1L, updateRequest);

            assertThat(result).isNotNull();
            verify(cartRepository).findActiveCartWithItems(1L);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowException_whenProductNotFound() {
            when(cartValidator.validateProduct(1L))
                    .thenThrow(new ResourceNotFoundException("Product not found"));

            assertThatThrownBy(() -> cartService.updateCartItem(1L, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when product not active")
        void shouldThrowException_whenProductNotActive() {
            when(cartValidator.validateProduct(1L))
                    .thenThrow(new ProductNotActiveException(testProduct.getId(), testProduct.getSku()));

            assertThatThrownBy(() -> cartService.updateCartItem(1L, updateRequest))
                    .isInstanceOf(ProductNotActiveException.class);
        }

        @Test
        @DisplayName("Should throw exception when no active cart found")
        void shouldThrowException_whenNoActiveCart() {
            when(cartValidator.validateProduct(1L)).thenReturn(testProduct);
            when(cartRepository.findActiveCartWithItems(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.updateCartItem(1L, updateRequest))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("No active cart found");
        }
    }

    // Delete-from-cart tests

    @Nested
    @DisplayName("Remove from Cart Tests")
    class RemoveFromCartTests {

        @Test
        @DisplayName("Should remove product from cart")
        void shouldRemoveProductFromCart() {
            when(cartValidator.validateProduct(1L)).thenReturn(testProduct);
            when(cartRepository.findActiveCartWithItems(1L)).thenReturn(Optional.of(testCart));
            when(cartMapper.toResponse(any(Cart.class))).thenReturn(testResponse);

            CartResponse result = cartService.removeFromCart(1L, 1L);

            assertThat(result).isNotNull();
            verify(cartRepository).findActiveCartWithItems(1L);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowException_whenProductNotFound() {
            when(cartValidator.validateProduct(999L))
                    .thenThrow(new ResourceNotFoundException("Product not found"));

            assertThatThrownBy(() -> cartService.removeFromCart(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when no active cart found")
        void shouldThrowException_whenNoActiveCart() {
            when(cartValidator.validateProduct(1L)).thenReturn(testProduct);
            when(cartRepository.findActiveCartWithItems(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.removeFromCart(1L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active cart found for user");
        }
    }

    // Query tests

    @Nested
    @DisplayName("Get Cart Tests")
    class GetCartTests {

        @Test
        @DisplayName("Should return active cart when exists")
        void shouldReturnActiveCart_whenExists() {
            when(cartRepository.findActiveCartByUserId(1L)).thenReturn(Optional.of(testCart));
            when(cartMapper.toResponse(any(Cart.class))).thenReturn(testResponse);

            CartResponse result = cartService.getActiveCart(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should create new cart when no active cart exists")
        void shouldCreateNewCart_whenNoActiveCart() {
            when(cartValidator.validateUser(1L)).thenReturn(testUser);
            when(cartRepository.findActiveCartByUserId(1L)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
            when(cartMapper.toResponse(any(Cart.class))).thenReturn(testResponse);

            CartResponse result = cartService.getActiveCart(1L);

            assertThat(result).isNotNull();
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("Should return active cart with items")
        void shouldReturnActiveCartWithItems() {
            when(cartRepository.findActiveCartWithItems(1L)).thenReturn(Optional.of(testCart));
            when(cartMapper.toResponse(any(Cart.class))).thenReturn(testResponse);

            CartResponse result = cartService.getActiveCartWithItems(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return total items count")
        void shouldReturnTotalItemsCount() {
            when(cartRepository.findActiveCartByUserId(1L)).thenReturn(Optional.of(testCart));

            int totalItems = cartService.getTotalItems(1L);

            assertThat(totalItems).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return 0 when no active cart for total items")
        void shouldReturnZero_whenNoActiveCart() {
            when(cartRepository.findActiveCartByUserId(1L)).thenReturn(Optional.empty());

            int totalItems = cartService.getTotalItems(1L);

            assertThat(totalItems).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return total amount")
        void shouldReturnTotalAmount() {
            when(cartRepository.findActiveCartByUserId(1L)).thenReturn(Optional.of(testCart));

            java.math.BigDecimal total = cartService.getTotalAmount(1L);

            assertThat(total).isNotNull();
        }

        @Test
        @DisplayName("Should return zero when no active cart for total amount")
        void shouldReturnZero_whenNoActiveCartForAmount() {
            when(cartRepository.findActiveCartByUserId(1L)).thenReturn(Optional.empty());

            java.math.BigDecimal total = cartService.getTotalAmount(1L);

            assertThat(total).isEqualTo(java.math.BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should check if cart has items")
        void shouldCheckIfCartHasItems() {
            when(cartRepository.findActiveCartByUserId(1L)).thenReturn(Optional.of(testCart));

            boolean hasItems = cartService.hasItems(1L);

            assertThat(hasItems).isTrue();
        }

        @Test
        @DisplayName("Should return false when no active cart for hasItems")
        void shouldReturnFalse_whenNoActiveCartForHasItems() {
            when(cartRepository.findActiveCartByUserId(1L)).thenReturn(Optional.empty());

            boolean hasItems = cartService.hasItems(1L);

            assertThat(hasItems).isFalse();
        }
    }

    // Cart emptying tests

    @Nested
    @DisplayName("Clear Cart Tests")
    class ClearCartTests {

        @Test
        @DisplayName("Should clear cart when active cart exists")
        void shouldClearCart_whenActiveCartExists() {
            when(cartRepository.findActiveCartWithItems(1L)).thenReturn(Optional.of(testCart));

            cartService.clearCart(1L);

            assertThat(testCart.getItems()).isEmpty();
            verify(cartRepository).findActiveCartWithItems(1L);
        }

        @Test
        @DisplayName("Should throw exception when no active cart found")
        void shouldThrowException_whenNoActiveCartForClear() {
            when(cartRepository.findActiveCartWithItems(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.clearCart(1L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("No active cart found");
        }
    }
}