package com.spring.luispa.ecommerce_api.integration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.luispa.ecommerce_api.api.dto.request.*;
import com.spring.luispa.ecommerce_api.api.dto.response.JwtResponse;
import com.spring.luispa.ecommerce_api.domain.cart.CartRepository;
import com.spring.luispa.ecommerce_api.domain.order.OrderRepository;
import com.spring.luispa.ecommerce_api.domain.payment.Payment;
import com.spring.luispa.ecommerce_api.domain.payment.PaymentRepository;
import com.spring.luispa.ecommerce_api.domain.product.Category;
import com.spring.luispa.ecommerce_api.domain.product.CategoryRepository;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.product.ProductRepository;
import com.spring.luispa.ecommerce_api.domain.user.Address;
import com.spring.luispa.ecommerce_api.domain.user.AddressRepository;
import com.spring.luispa.ecommerce_api.domain.user.Role;
import com.spring.luispa.ecommerce_api.domain.user.RoleRepository;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.domain.user.UserRepository;
import com.spring.luispa.ecommerce_api.shared.enums.PaymentMethod;
import com.spring.luispa.ecommerce_api.shared.enums.PaymentStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("PaymentController Integration Tests")
class PaymentControllerIntegrationTest {

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
    private PaymentRepository paymentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;
    private Long userId;
    private String adminAccessToken;
    private Product testProduct;
    private Address testAddress;
    private CreateOrderRequest createOrderRequest;
    private ProcessPaymentRequest processPaymentRequest;

    @BeforeEach
    void setUp() throws Exception {
        Role adminRoleDb = new  Role(RoleName.ROLE_ADMIN);
        Role userRoleDb = new  Role(RoleName.ROLE_USER);

        roleRepository.save(adminRoleDb);
        roleRepository.save(userRoleDb);

        // Register a regular user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Register an admin user
        RegisterRequest adminRegisterRequest = new RegisterRequest();
        adminRegisterRequest.setEmail("admin@example.com");
        adminRegisterRequest.setPassword("admin123");
        adminRegisterRequest.setFirstName("Admin");
        adminRegisterRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegisterRequest)))
                .andExpect(status().isCreated());

        // Assign ADMIN role to admin user
        User adminUser = userRepository.findByEmail("admin@example.com").orElseThrow();
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> {
                    Role newRole = new Role(RoleName.ROLE_ADMIN);
                    newRole.setDescription("Administrator role");
                    return roleRepository.save(newRole);
                });
        adminUser.addRole(adminRole);
        userRepository.save(adminUser);

        // Login as a normal user
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

        // Login as admin
        LoginRequest adminLoginRequest = new LoginRequest();
        adminLoginRequest.setEmail("admin@example.com");
        adminLoginRequest.setPassword("admin123");

        String adminLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtResponse adminJwtResponse = objectMapper.readValue(adminLoginResponse, JwtResponse.class);
        adminAccessToken = adminJwtResponse.getToken();

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

        // Create order request
        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setShippingAddressId(testAddress.getId());
        createOrderRequest.setBillingAddressId(testAddress.getId());
        createOrderRequest.setShippingMethod("standard");

        // Create payment request
        processPaymentRequest = new ProcessPaymentRequest();
        processPaymentRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        processPaymentRequest.setCurrency("USD");
        processPaymentRequest.setCardLastFour("4242");
        processPaymentRequest.setCardBrand("VISA");
    }

    // Helpers

    private Long createOrder() throws Exception {
        // Add to the cart
        AddToCartRequest addRequest = new AddToCartRequest();
        addRequest.setProductId(testProduct.getId());
        addRequest.setQuantity(2);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk());

        // Create orden
        String orderResponse = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(orderResponse).get("id").asLong();
    }

    // POST /api/payments/order/{orderId}/process

    @Nested
    @DisplayName("POST /api/payments/order/{orderId}/process")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Should process payment successfully")
        void shouldProcessPayment_successfully() throws Exception {
            Long orderId = createOrder();

            mockMvc.perform(post("/api/payments/order/{orderId}/process", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(processPaymentRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.orderId").value(orderId))
                    .andExpect(jsonPath("$.amount").exists());

            // Verify on DB
            Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
            assertThat(payment).isNotNull();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should return 400 when order already has payment")
        void shouldReturn400_whenOrderAlreadyHasPayment() throws Exception {
            Long orderId = createOrder();

            // Process payment (first time)
            mockMvc.perform(post("/api/payments/order/{orderId}/process", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(processPaymentRequest)))
                    .andExpect(status().isOk());

            // Try to pay again
            mockMvc.perform(post("/api/payments/order/{orderId}/process", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(processPaymentRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
        }

        @Test
        @DisplayName("Should return 400 when order is not pending")
        void shouldReturn400_whenOrderIsNotPending() throws Exception {
            Long orderId = createOrder();

            // Process payment
            mockMvc.perform(post("/api/payments/order/{orderId}/process", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(processPaymentRequest)))
                    .andExpect(status().isOk());

            // Try to pay again
            mockMvc.perform(post("/api/payments/order/{orderId}/process", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(processPaymentRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
        }

        @Test
        @DisplayName("Should return 404 when order not found")
        void shouldReturn404_whenOrderNotFound() throws Exception {
            mockMvc.perform(post("/api/payments/order/{orderId}/process", 99999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(processPaymentRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }
    }

    // GET /api/payments/order/{orderId}

    @Nested
    @DisplayName("GET /api/payments/order/{orderId}")
    class GetPaymentByOrderTests {

        @Test
        @DisplayName("Should return payment when exists")
        void shouldReturnPayment_whenExists() throws Exception {
            Long orderId = createOrder();

            // Process payment
            mockMvc.perform(post("/api/payments/order/{orderId}/process", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(processPaymentRequest)))
                    .andExpect(status().isOk());

            // Get paymnet
            mockMvc.perform(get("/api/payments/order/{orderId}", orderId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(orderId))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("Should return 404 when payment not found")
        void shouldReturn404_whenPaymentNotFound() throws Exception {
            Long orderId = createOrder();

            mockMvc.perform(get("/api/payments/order/{orderId}", orderId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }
    }

    // GET /api/payments/user

    @Nested
    @DisplayName("GET /api/payments/user")
    class GetUserPaymentsTests {

        @Test
        @DisplayName("Should return user payments")
        void shouldReturnUserPayments() throws Exception {
            Long orderId = createOrder();

            // Process payment
            String paymentResponse = mockMvc.perform(post("/api/payments/order/{orderId}/process", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(processPaymentRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            System.out.println("Payment Response: " + paymentResponse);

            mockMvc.perform(get("/api/payments/user")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].orderId").value(orderId));
        }

        @Test
        @DisplayName("Should return empty list when no payments")
        void shouldReturnEmptyList_whenNoPayments() throws Exception {
            mockMvc.perform(get("/api/payments/user")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // POST /api/payments/{id}/refund (ADMIN)

    @Nested
    @DisplayName("POST /api/payments/{id}/refund (ADMIN)")
    class RefundPaymentTests {

        @Test
        @DisplayName("Should refund payment when admin is authenticated")
        void shouldRefundPayment_whenAdminAuthenticated() throws Exception {
            Long orderId = createOrder();

            // Process payment
            String paymentResponse = mockMvc.perform(post("/api/payments/order/{orderId}/process", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(processPaymentRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long paymentId = objectMapper.readTree(paymentResponse).get("id").asLong();

            // Refund
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setReason("Customer requested refund");

            mockMvc.perform(post("/api/payments/{id}/refund", paymentId)
                            .header("Authorization", "Bearer " + adminAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refundRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REFUNDED"));
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        void shouldReturn403_whenUserIsNotAdmin() throws Exception {
            Long orderId = createOrder();

            // Process payment
            String paymentResponse = mockMvc.perform(post("/api/payments/order/{orderId}/process", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(processPaymentRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long paymentId = objectMapper.readTree(paymentResponse).get("id").asLong();

            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setReason("Customer requested refund");

            // Normal user try to refund
            mockMvc.perform(post("/api/payments/{id}/refund", paymentId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refundRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 400 when payment is not refundable")
        void shouldReturn400_whenPaymentNotRefundable() throws Exception {
            Long orderId = createOrder();

            // Process payment
            String paymentResponse = mockMvc.perform(post("/api/payments/order/{orderId}/process", orderId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(processPaymentRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long paymentId = objectMapper.readTree(paymentResponse).get("id").asLong();

            // Refund the first time
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setReason("Customer requested refund");

            mockMvc.perform(post("/api/payments/{id}/refund", paymentId)
                            .header("Authorization", "Bearer " + adminAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refundRequest)))
                    .andExpect(status().isOk());

            // Try to refund again
            mockMvc.perform(post("/api/payments/{id}/refund", paymentId)
                            .header("Authorization", "Bearer " + adminAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refundRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
        }
    }
}