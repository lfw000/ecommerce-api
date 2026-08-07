package com.spring.luispa.ecommerce_api.integration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.luispa.ecommerce_api.api.dto.request.*;
import com.spring.luispa.ecommerce_api.api.dto.response.JwtResponse;
import com.spring.luispa.ecommerce_api.domain.cart.CartRepository;
import com.spring.luispa.ecommerce_api.domain.order.OrderRepository;
import com.spring.luispa.ecommerce_api.domain.product.Category;
import com.spring.luispa.ecommerce_api.domain.product.CategoryRepository;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.product.ProductRepository;
import com.spring.luispa.ecommerce_api.domain.user.*;
import com.spring.luispa.ecommerce_api.services.CartService;
import com.spring.luispa.ecommerce_api.shared.enums.CancellationReason;
import com.spring.luispa.ecommerce_api.shared.enums.RoleName;
import com.spring.luispa.ecommerce_api.test.helpers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderController Integration Tests")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    private String accessToken;
    private Long userId;
    private Product testProduct;
    private Address testAddress;
    private CreateOrderRequest createOrderRequest;
    @Autowired
    private CartService cartService;

    @BeforeEach
    void setUp() throws Exception {
        Role adminRole = new Role(RoleName.ROLE_ADMIN);
        Role userRole = new Role(RoleName.ROLE_USER);

        roleRepository.save(adminRole);
        roleRepository.save(userRole);

        // Register user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login to get the access token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtResponse jwtResponse = objectMapper.readValue(loginResponse, JwtResponse.class);
        accessToken = jwtResponse.getToken();
        userId = jwtResponse.getId();

        // Create address
        User user = userRepository.findById(userId).orElseThrow();
        testAddress = AddressTestHelper.newAddress();
        testAddress.setUser(user);
        testAddress = addressRepository.save(testAddress);

        // Create category and product
        Category category = categoryRepository.save(CategoryTestHelper.newCategory("Electrónicos"));
        testProduct = ProductTestHelper.newProductWithCategory(category);
        testProduct.setStock(10);
        testProduct = productRepository.save(testProduct);

        // Prepare order creation request
        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setShippingAddressId(testAddress.getId());
        createOrderRequest.setBillingAddressId(testAddress.getId());
        createOrderRequest.setShippingMethod("standard");
    }

    // POST /api/orders

    @Nested
    @DisplayName("POST /api/orders")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order when cart has items")
        void shouldCreateOrder_whenCartHasItems() throws Exception {
            // Add product to the cart
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(2);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            // Create order
            mockMvc.perform(post("/api/orders")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items[0].productId").value(testProduct.getId()))
                    .andExpect(jsonPath("$.items[0].quantity").value(2));

            // Verify if the cart was cleared
            mockMvc.perform(get("/api/cart")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isEmpty());
        }

        @Test
        @DisplayName("Should return 404 when user has no cart")
        void shouldReturn404_whenUserHasNoCart() throws Exception {
            mockMvc.perform(post("/api/orders")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 400 when cart is empty")
        void shouldReturn400_whenCartIsEmpty() throws Exception {
            cartService.getActiveCart(userId);

            mockMvc.perform(post("/api/orders")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
        }

        @Test
        @DisplayName("Should return 400 when address not found")
        void shouldReturn400_whenAddressNotFound() throws Exception {
            // Add product to the cart
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(1);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            createOrderRequest.setShippingAddressId(99999L);

            mockMvc.perform(post("/api/orders")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when not authenticated")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // GET /api/orders

    @Nested
    @DisplayName("GET /api/orders")
    class GetOrdersTests {

        @Test
        @DisplayName("Should return user orders when authenticated")
        void shouldReturnUserOrders_whenAuthenticated() throws Exception {
            // Add product to the cart
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(1);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            // Create order
            mockMvc.perform(post("/api/orders")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderRequest)))
                    .andExpect(status().isCreated());

            // Verify orders
            mockMvc.perform(get("/api/orders")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("Should return empty list when user has no orders")
        void shouldReturnEmptyList_whenUserHasNoOrders() throws Exception {
            mockMvc.perform(get("/api/orders")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ============================================================
    // GET /api/orders/{id}
    // ============================================================

    @Nested
    @DisplayName("GET /api/orders/{id}")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return order when user is owner")
        void shouldReturnOrder_whenUserIsOwner() throws Exception {
            // Add product and create order
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(1);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            String orderResponse = mockMvc.perform(post("/api/orders")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderRequest)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

            // Get order
            mockMvc.perform(get("/api/orders/{id}", orderId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId))
                    .andExpect(jsonPath("$.userId").value(userId));
        }

        @Test
        @DisplayName("Should return 404 when order not found")
        void shouldReturn404_whenOrderNotFound() throws Exception {
            mockMvc.perform(get("/api/orders/99999")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }
    }

    // POST /api/orders/{id}/cancel

    @Nested
    @DisplayName("POST /api/orders/{id}/cancel")
    class CancelOrderTests {

        private Long orderId;

        @BeforeEach
        void setUp() throws Exception {
            // Create order to cancel
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(1);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            String orderResponse = mockMvc.perform(post("/api/orders")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderRequest)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            orderId = objectMapper.readTree(orderResponse).get("id").asLong();
        }

        @Test
        @DisplayName("Should cancel order when user is owner")
        void shouldCancelOrder_whenUserIsOwner() throws Exception {
            CancelOrderRequest cancelRequest = new CancelOrderRequest();
            cancelRequest.setReason(CancellationReason.USER_REQUESTED);
            cancelRequest.setComment("Changed my mind");

            mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelRequest)))
                    .andExpect(status().isOk());

            // Verify if the order was canceled
            mockMvc.perform(get("/api/orders/{id}", orderId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("Should return 409 when order is already cancelled")
        void shouldReturn400_whenOrderAlreadyCancelled() throws Exception {
            // Cancel order
            CancelOrderRequest cancelRequest = new CancelOrderRequest();
            cancelRequest.setReason(CancellationReason.USER_REQUESTED);
            cancelRequest.setComment("Changed my mind");

            mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelRequest)))
                    .andExpect(status().isOk());

            // Try to cancel again
            mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelRequest)))
                    .andExpect(status().isConflict());
        }
    }
}