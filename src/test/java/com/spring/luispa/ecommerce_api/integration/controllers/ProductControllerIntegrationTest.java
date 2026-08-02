package com.spring.luispa.ecommerce_api.integration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.luispa.ecommerce_api.api.dto.request.CreateProductRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateProductRequest;
import com.spring.luispa.ecommerce_api.domain.product.Category;
import com.spring.luispa.ecommerce_api.domain.product.CategoryRepository;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.product.ProductRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductController Integration Tests")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private CreateProductRequest createRequest;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Create a new category (without ID)
        testCategory = categoryRepository.save(CategoryTestHelper.newCategory());

        createRequest = new CreateProductRequest();
        createRequest.setSku("LAP-INT-001");
        createRequest.setName("Laptop Integration");
        createRequest.setDescription("Laptop para testing de integración");
        createRequest.setPrice(new BigDecimal("1599.99"));
        createRequest.setCategoryId(testCategory.getId());
        createRequest.setStock(10);
        createRequest.setFeatured(true);

        // Clear products before each test
        productRepository.deleteAll();
    }

    // POST /api/products

    @Nested
    @DisplayName("POST /api/products")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product when admin is authenticated")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateProduct_whenAdminAuthenticated() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sku").value("LAP-INT-001"))
                    .andExpect(jsonPath("$.name").value("Laptop Integration"))
                    .andExpect(jsonPath("$.price").value(1599.99))
                    .andExpect(jsonPath("$.stock").value(10))
                    .andExpect(jsonPath("$.featured").value(true));

            Product savedProduct = productRepository.findBySku("LAP-INT-001").orElse(null);
            assertThat(savedProduct).isNotNull();
            assertThat(savedProduct.getName()).isEqualTo("Laptop Integration");
        }

        @Test
        @DisplayName("Should return 409 when SKU already exists")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn409_whenSkuAlreadyExists() throws Exception {
            // Create an existing product using the helper object
            Product existingProduct = ProductTestHelper.newProduct("LAP-INT-001", "Existing Laptop");
            existingProduct.setCategory(testCategory);
            productRepository.save(existingProduct);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"));
        }

        @Test
        @DisplayName("Should return 403 when not authenticated")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotAdmin() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // GET /api/products

    @Nested
    @DisplayName("GET /api/products")
    class GetProductTests {

        @Test
        @DisplayName("Should return product by ID when exists")
        void shouldReturnProductById_whenExists() throws Exception {
            // Create product using the helper object
            Product product = ProductTestHelper.newProduct();
            product.setCategory(testCategory);
            Product saved = productRepository.save(product);

            mockMvc.perform(get("/api/products/{id}", saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId()))
                    .andExpect(jsonPath("$.sku").value("LAP-INT-001"))
                    .andExpect(jsonPath("$.name").value("Laptop Integration"))
                    .andExpect(jsonPath("$.price").value(1599.99));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404_whenProductNotFound() throws Exception {
            mockMvc.perform(get("/api/products/99999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return only active products")
        void shouldReturnOnlyActiveProducts() throws Exception {
            // Create products using the helper object
            Product active = ProductTestHelper.newProduct();
            active.setCategory(testCategory);
            active.setSku("LAP-ACTIVE-001");
            productRepository.save(active);

            Product inactive = ProductTestHelper.newInactiveProduct();
            inactive.setCategory(testCategory);
            productRepository.save(inactive);

            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].sku").value("LAP-ACTIVE-001"));
        }
    }

    // GET /api/products/search

    @Nested
    @DisplayName("GET /api/products/search")
    class SearchProductTests {

        @Test
        @DisplayName("Should search products by keyword")
        void shouldSearchProducts_byKeyword() throws Exception {
            // Create products using the helper object
            Product laptop = ProductTestHelper.newProduct("LAP-001", "Gaming Laptop");
            laptop.setCategory(testCategory);
            productRepository.save(laptop);

            Product mouse = ProductTestHelper.newProduct("MOU-001", "Wireless Mouse");
            mouse.setCategory(testCategory);
            productRepository.save(mouse);

            mockMvc.perform(get("/api/products/search")
                            .param("keyword", "laptop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].sku").value("LAP-001"));
        }
    }

    // PUT /api/products/{id}

    @Nested
    @DisplayName("PUT /api/products/{id}")
    class UpdateProductTests {

        private UpdateProductRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new UpdateProductRequest();
            updateRequest.setName("Updated Laptop");
            updateRequest.setPrice(new BigDecimal("1999.99"));
            updateRequest.setStock(5);
        }

        @Test
        @DisplayName("Should update product when admin is authenticated")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateProduct_whenAdminAuthenticated() throws Exception {
            // Create product using the helper object
            Product product = ProductTestHelper.newProduct();
            product.setCategory(testCategory);
            Product saved = productRepository.save(product);

            mockMvc.perform(put("/api/products/{id}", saved.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId()))
                    .andExpect(jsonPath("$.name").value("Updated Laptop"))
                    .andExpect(jsonPath("$.price").value(1999.99))
                    .andExpect(jsonPath("$.stock").value(5));

            Product updated = productRepository.findById(saved.getId()).orElse(null);
            assertThat(updated).isNotNull();
            assertThat(updated.getName()).isEqualTo("Updated Laptop");
            assertThat(updated.getPrice()).isEqualTo(new BigDecimal("1999.99"));
            assertThat(updated.getStock()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404_whenProductNotFound() throws Exception {
            mockMvc.perform(put("/api/products/99999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        @WithMockUser(roles = "USER")
        void shouldReturn403_whenUserIsNotAdmin() throws Exception {
            Product product = ProductTestHelper.newProduct();
            product.setCategory(testCategory);
            Product saved = productRepository.save(product);

            mockMvc.perform(put("/api/products/{id}", saved.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // DELETE /api/products/{id}

    @Nested
    @DisplayName("DELETE /api/products/{id}")
    class DeleteProductTests {

        @Test
        @DisplayName("Should soft delete product when admin is authenticated")
        @WithMockUser(roles = "ADMIN")
        void shouldSoftDeleteProduct_whenAdminAuthenticated() throws Exception {
            // Create product using the helper object
            Product product = ProductTestHelper.newProduct();
            product.setCategory(testCategory);
            Product saved = productRepository.save(product);

            mockMvc.perform(delete("/api/products/{id}", saved.getId()))
                    .andExpect(status().isNoContent());

            Product deleted = productRepository.findById(saved.getId()).orElse(null);
            assertThat(deleted).isNotNull();
            assertThat(deleted.getActive()).isFalse();
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404_whenProductNotFound() throws Exception {
            mockMvc.perform(delete("/api/products/99999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }
    }

    // Pagination

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should return paginated results")
        void shouldReturnPaginatedResults() throws Exception {
            // Create 25 products using the helper object
            for (int i = 1; i <= 25; i++) {
                Product product = ProductTestHelper.newProduct("SKU-" + i, "Product " + i);
                product.setCategory(testCategory);
                product.setPrice(new BigDecimal(i * 10.0));
                productRepository.save(product);
            }

            mockMvc.perform(get("/api/products")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.totalElements").value(25))
                    .andExpect(jsonPath("$.totalPages").value(3))
                    .andExpect(jsonPath("$.first").value(true))
                    .andExpect(jsonPath("$.last").value(false));
        }

        @Test
        @DisplayName("Should return second page with correct elements")
        void shouldReturnSecondPageWithCorrectElements() throws Exception {
            // Create 15 products using the helper object
            for (int i = 1; i <= 15; i++) {
                Product product = ProductTestHelper.newProduct("SKU-" + i, "Product " + i);
                product.setCategory(testCategory);
                product.setPrice(new BigDecimal(i * 10.0));
                productRepository.save(product);
            }

            mockMvc.perform(get("/api/products")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(5)))
                    .andExpect(jsonPath("$.totalElements").value(15))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.first").value(false))
                    .andExpect(jsonPath("$.last").value(true));
        }

        @Test
        @DisplayName("Should return empty page when page exceeds total")
        void shouldReturnEmptyPage_whenPageExceedsTotal() throws Exception {
            mockMvc.perform(get("/api/products")
                            .param("page", "999")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }
}