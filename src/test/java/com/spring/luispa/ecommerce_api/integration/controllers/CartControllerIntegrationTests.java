package com.spring.luispa.ecommerce_api.integration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.luispa.ecommerce_api.api.dto.request.AddToCartRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.LoginRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RegisterRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateCartItemRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.JwtResponse;
import com.spring.luispa.ecommerce_api.domain.cart.Cart;
import com.spring.luispa.ecommerce_api.domain.cart.CartRepository;
import com.spring.luispa.ecommerce_api.domain.product.Category;
import com.spring.luispa.ecommerce_api.domain.product.CategoryRepository;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.product.ProductRepository;
import com.spring.luispa.ecommerce_api.domain.user.Role;
import com.spring.luispa.ecommerce_api.domain.user.RoleRepository;
import com.spring.luispa.ecommerce_api.shared.enums.RoleName;
import com.spring.luispa.ecommerce_api.test.helpers.CategoryTestHelper;
import com.spring.luispa.ecommerce_api.test.helpers.ProductTestHelper;
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
@DisplayName("CartController Integration Tests")
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    private String accessToken;
    private Long userId;
    private Product testProduct;


    @BeforeEach
    void setUp() throws Exception {
        Role user = new Role(RoleName.ROLE_USER);
        Role admin = new Role(RoleName.ROLE_ADMIN);

        roleRepository.save(user);
        roleRepository.save(admin);

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

        // Create category and product
        Category category = categoryRepository.save(CategoryTestHelper.newCategory("Electrónicos"));
        testProduct = ProductTestHelper.newProductWithCategory(category);
        testProduct.setStock(10);
        testProduct = productRepository.save(testProduct);
    }

    // GET /api/cart

    @Nested
    @DisplayName("GET /api/cart")
    class GetCartTests {

        @Test
        @DisplayName("Should return empty cart when user has no items")
        void shouldReturnEmptyCart_whenUserHasNoItems() throws Exception {
            mockMvc.perform(get("/api/cart")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isEmpty())
                    .andExpect(jsonPath("$.totalAmount").value(0))
                    .andExpect(jsonPath("$.totalItems").value(0))
                    .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        @DisplayName("Should return cart with items after adding products")
        void shouldReturnCartWithItems_afterAddingProducts() throws Exception {
            // Add product to the cart
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(2);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            // Verify cart
            mockMvc.perform(get("/api/cart")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items[0].productId").value(testProduct.getId()))
                    .andExpect(jsonPath("$.items[0].quantity").value(2))
                    .andExpect(jsonPath("$.totalItems").value(2));
        }
    }

    // POST /api/cart/items

    @Nested
    @DisplayName("POST /api/cart/items")
    class AddToCartTests {

        @Test
        @DisplayName("Should add product to cart when product exists and has stock")
        void shouldAddProductToCart_whenProductExists() throws Exception {
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(3);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].productId").value(testProduct.getId()))
                    .andExpect(jsonPath("$.items[0].quantity").value(3))
                    .andExpect(jsonPath("$.totalItems").value(3));

            // Check on database
            Cart cart = cartRepository.findActiveCartByUserId(userId).orElse(null);
            assertThat(cart).isNotNull();
            assertThat(cart.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("Should increment quantity when product already in cart")
        void shouldIncrementQuantity_whenProductAlreadyInCart() throws Exception {
            // First addition
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(2);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            // Second addition (same product)
            addRequest.setQuantity(3);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].quantity").value(5))
                    .andExpect(jsonPath("$.totalItems").value(5));
        }

        @Test
        @DisplayName("Should return 400 when product not found")
        void shouldReturn400_whenProductNotFound() throws Exception {
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(99999L);
            addRequest.setQuantity(1);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(1);

            mockMvc.perform(post("/api/cart/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // PUT /api/cart/items

    @Nested
    @DisplayName("PUT /api/cart/items")
    class UpdateCartItemTests {

        @Test
        @DisplayName("Should update quantity when product in cart")
        void shouldUpdateQuantity_whenProductInCart() throws Exception {
            // Add product
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(2);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            // Update quantity
            UpdateCartItemRequest updateRequest = new UpdateCartItemRequest();
            updateRequest.setProductId(testProduct.getId());
            updateRequest.setQuantity(5);

            mockMvc.perform(put("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].quantity").value(5))
                    .andExpect(jsonPath("$.totalItems").value(5));
        }

        @Test
        @DisplayName("Should remove item when quantity is 0")
        void shouldRemoveItem_whenQuantityIsZero() throws Exception {
            // Add product
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(2);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            // Update to 0 (delete)
            UpdateCartItemRequest updateRequest = new UpdateCartItemRequest();
            updateRequest.setProductId(testProduct.getId());
            updateRequest.setQuantity(0);

            mockMvc.perform(put("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isEmpty())
                    .andExpect(jsonPath("$.totalItems").value(0));
        }
    }

    // DELETE /api/cart/items/{productId}

    @Nested
    @DisplayName("DELETE /api/cart/items/{productId}")
    class RemoveFromCartTests {

        @Test
        @DisplayName("Should remove product from cart")
        void shouldRemoveProductFromCart() throws Exception {
            // Add product
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(2);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            // Remove product
            mockMvc.perform(delete("/api/cart/items/{productId}", testProduct.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.items").isEmpty())
                    .andExpect(jsonPath("$.totalItems").value(0));
        }

        @Test
        @DisplayName("Should return 404 when product does not exist")
        void shouldReturn200_whenProductNotInCart() throws Exception {
            mockMvc.perform(delete("/api/cart/items/{productId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 204 when product is not in cart")
        void shouldReturn204_whenProductNotInCart() throws Exception {
            // Product exists (testProduct), but never was added to the cart
            mockMvc.perform(delete("/api/cart/items/{productId}", testProduct.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNoContent());
        }
    }

    // DELETE /api/cart

    @Nested
    @DisplayName("DELETE /api/cart")
    class ClearCartTests {

        @Test
        @DisplayName("Should clear all items from cart")
        void shouldClearAllItemsFromCart() throws Exception {
            // Add product
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(2);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            // Clear cart
            mockMvc.perform(delete("/api/cart")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNoContent());

            // Verify if the cart is empty
            mockMvc.perform(get("/api/cart")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isEmpty())
                    .andExpect(jsonPath("$.totalItems").value(0));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/cart"))
                    .andExpect(status().isForbidden());
        }
    }

    // GET /api/cart/count

    @Nested
    @DisplayName("GET /api/cart/count")
    class GetCartCountTests {

        @Test
        @DisplayName("Should return total items count")
        void shouldReturnTotalItemsCount() throws Exception {
            // Add product
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(3);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/cart/count")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string("3"));
        }

        @Test
        @DisplayName("Should return 0 when cart is empty")
        void shouldReturnZero_whenCartIsEmpty() throws Exception {
            mockMvc.perform(get("/api/cart/count")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string("0"));
        }
    }

    // GET /api/cart/total

    @Nested
    @DisplayName("GET /api/cart/total")
    class GetCartTotalTests {

        @Test
        @DisplayName("Should return total amount")
        void shouldReturnTotalAmount() throws Exception {
            // Add product
            AddToCartRequest addRequest = new AddToCartRequest();
            addRequest.setProductId(testProduct.getId());
            addRequest.setQuantity(2);

            mockMvc.perform(post("/api/cart/items")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/cart/total")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isNumber());
        }

        @Test
        @DisplayName("Should return 0 when cart is empty")
        void shouldReturnZero_whenCartIsEmpty() throws Exception {
            mockMvc.perform(get("/api/cart/total")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string("0"));
        }
    }
}