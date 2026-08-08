package com.spring.luispa.ecommerce_api.unit.services;

import com.spring.luispa.ecommerce_api.api.dto.request.CreateProductRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateProductRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.ProductResponse;
import com.spring.luispa.ecommerce_api.domain.product.Category;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.product.ProductRepository;
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
import com.spring.luispa.ecommerce_api.mappers.ProductMapper;
import com.spring.luispa.ecommerce_api.services.ProductService;
import com.spring.luispa.ecommerce_api.services.validation.ProductValidator;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.DuplicateResourceException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import com.spring.luispa.ecommerce_api.test.helpers.CategoryTestHelper;
import com.spring.luispa.ecommerce_api.test.helpers.ProductTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    // Mocks

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductValidator productValidator;

    @Mock
    private LoggingAspect loggingAspect;

    private ProductService productService;

    // Test data

    private Category testCategory;
    private Product testProduct;
    private ProductResponse testResponse;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;

    @BeforeEach
    void setUp() {
        productService = new ProductService(
                productRepository,
                productMapper,
                productValidator,
                loggingAspect
        );

        testCategory = CategoryTestHelper.defaultCategory(1L);
        testProduct = ProductTestHelper.defaultProduct(1L);
        testProduct.setCategory(testCategory);

        testResponse = new ProductResponse();
        testResponse.setId(1L);
        testResponse.setSku("LAP-001");
        testResponse.setName("Laptop Gamer");
        testResponse.setPrice(new BigDecimal("1599.99"));
        testResponse.setCategoryId(1L);
        testResponse.setCategoryName("Electrónicos");

        createRequest = new CreateProductRequest();
        createRequest.setSku("LAP-001");
        createRequest.setName("Laptop Gamer");
        createRequest.setPrice(new BigDecimal("1599.99"));
        createRequest.setCategoryId(1L);
        createRequest.setStock(10);

        updateRequest = new UpdateProductRequest();
        updateRequest.setName("Laptop Ultra");
        updateRequest.setPrice(new BigDecimal("1999.99"));
        updateRequest.setStock(15);
    }

    // Creation tests

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product when valid data provided")
        void shouldCreateProduct_whenValidDataProvided() {
            doNothing().when(productValidator).validateSku(createRequest.getSku());
            doNothing().when(productValidator).validateSkuUniqueness(createRequest.getSku());
            doNothing().when(productValidator).validatePrice(createRequest.getPrice());
            doNothing().when(productValidator).validateStock(createRequest.getStock());
            when(productValidator.validateCategory(1L)).thenReturn(testCategory);

            when(productMapper.toEntity(createRequest)).thenReturn(testProduct);
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            when(productMapper.toResponse(any(Product.class))).thenReturn(testResponse);

            ProductResponse result = productService.createProduct(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSku()).isEqualTo("LAP-001");
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when SKU already exists")
        void shouldThrowException_whenSkuAlreadyExists() {
            doThrow(new DuplicateResourceException("Product already exists with SKU: LAP-001"))
                    .when(productValidator).validateSkuUniqueness(createRequest.getSku());

            assertThatThrownBy(() -> productService.createProduct(createRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("already exists with SKU");
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowException_whenCategoryNotFound() {
            createRequest.setCategoryId(999L);
            when(productValidator.validateCategory(999L))
                    .thenThrow(new ResourceNotFoundException("Category not found with id: 999"));

            assertThatThrownBy(() -> productService.createProduct(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category not found");
            verify(productRepository, never()).save(any());
        }
    }

    // Update tests

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product when valid data provided")
        void shouldUpdateProduct_whenValidDataProvided() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            doNothing().when(productValidator).validatePrice(updateRequest.getPrice());
            doNothing().when(productValidator).validateStock(updateRequest.getStock());
            when(productMapper.toResponse(any(Product.class))).thenReturn(testResponse);

            ProductResponse result = productService.updateProduct(1L, updateRequest);

            assertThat(result).isNotNull();
            assertThat(testProduct.getName()).isEqualTo("Laptop Ultra");
            assertThat(testProduct.getPrice()).isEqualTo(new BigDecimal("1999.99"));
            assertThat(testProduct.getStock()).isEqualTo(15);
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowException_whenProductNotFound() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(999L, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            doNothing().when(productValidator).validateStock(updateRequest.getStock());
            when(productMapper.toResponse(any(Product.class))).thenReturn(testResponse);

            updateRequest.setName(null);
            updateRequest.setPrice(null);

            productService.updateProduct(1L, updateRequest);

            assertThat(testProduct.getName()).isEqualTo("Laptop Gamer");
            assertThat(testProduct.getPrice()).isEqualTo(new BigDecimal("1599.99"));
            assertThat(testProduct.getStock()).isEqualTo(15);
        }
    }

    // Delete tests

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should soft delete product when exists")
        void shouldSoftDeleteProduct_whenExists() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            productService.deleteProduct(1L);

            assertThat(testProduct.getActive()).isFalse();
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowException_whenProductNotFound() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // Stock management tests

    @Nested
    @DisplayName("Stock Management Tests")
    class StockManagementTests {

        @Test
        @DisplayName("Should update stock")
        void shouldUpdateStock() {
            doNothing().when(productValidator).validateStock(25);
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            productService.updateStock(1L, 25);

            assertThat(testProduct.getStock()).isEqualTo(25);
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Should adjust stock positively")
        void shouldAdjustStockPositively() {
            testProduct.setStock(10);
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            productService.adjustStock(1L, 5);

            assertThat(testProduct.getStock()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should adjust stock negatively")
        void shouldAdjustStockNegatively() {
            testProduct.setStock(10);
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            productService.adjustStock(1L, -3);

            assertThat(testProduct.getStock()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should throw exception when adjusting stock below zero")
        void shouldThrowException_whenAdjustingStockBelowZero() {
            testProduct.setStock(10);
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            assertThatThrownBy(() -> productService.adjustStock(1L, -15))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Stock cannot be negative");
        }

        @Test
        @DisplayName("Should throw exception when product not found for stock update")
        void shouldThrowException_whenProductNotFoundForStockUpdate() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateStock(999L, 10))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when stock is negative")
        void shouldThrowException_whenStockIsNegative() {
            doThrow(new IllegalArgumentException("Stock cannot be negative"))
                    .when(productValidator).validateStock(-5);

            assertThatThrownBy(() -> productService.updateStock(1L, -5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock cannot be negative");
        }
    }

    // Query tests

    @Nested
    @DisplayName("Query Tests")
    class QueryTests {

        @Test
        @DisplayName("Should return product by ID when exists")
        void shouldReturnProductById_whenExists() {
            when(productRepository.findActiveWithCategoryAndImagesById(1L)).thenReturn(Optional.of(testProduct));
            when(productMapper.toResponse(any(Product.class))).thenReturn(testResponse);

            ProductResponse result = productService.findById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(productRepository).findActiveWithCategoryAndImagesById(1L);
        }

        @Test
        @DisplayName("Should throw exception when product not found by ID")
        void shouldThrowException_whenProductNotFoundById() {
            when(productRepository.findActiveWithCategoryAndImagesById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(productRepository).findActiveWithCategoryAndImagesById(999L);
        }

        @Test
        @DisplayName("Should return product by SKU when exists")
        void shouldReturnProductBySku_whenExists() {
            when(productRepository.findActiveBySku("LAP-001")).thenReturn(Optional.of(testProduct));
            when(productMapper.toResponse(any(Product.class))).thenReturn(testResponse);

            ProductResponse result = productService.findBySku("LAP-001");

            assertThat(result).isNotNull();
            assertThat(result.getSku()).isEqualTo("LAP-001");
            verify(productRepository).findActiveBySku("LAP-001");
        }

        @Test
        @DisplayName("Should throw exception when product not found by SKU")
        void shouldThrowException_whenProductNotFoundBySku() {
            when(productRepository.findActiveBySku("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findBySku("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should return all products")
        void shouldReturnAllProducts() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(testProduct));

            when(productRepository.findByActiveTrue(pageable)).thenReturn(productPage);
            when(productMapper.toResponse(any(Product.class))).thenReturn(testResponse);

            Page<ProductResponse> result = productService.findAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findByActiveTrue(pageable);
        }

        @Test
        @DisplayName("Should return products by category")
        void shouldReturnProductsByCategory() {
            when(productRepository.findByCategoryIdAndActiveTrue(1L)).thenReturn(List.of(testProduct));
            when(productMapper.toResponseList(anyList())).thenReturn(List.of(testResponse));

            List<ProductResponse> results = productService.findByCategory(1L);

            assertThat(results).hasSize(1);
            verify(productRepository).findByCategoryIdAndActiveTrue(1L);
        }

        @Test
        @DisplayName("Should search products")
        void shouldSearchProducts() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(testProduct));

            when(productRepository.searchActiveProducts("laptop", pageable)).thenReturn(productPage);
            when(productMapper.toResponse(any(Product.class))).thenReturn(testResponse);

            Page<ProductResponse> result = productService.searchProducts("laptop", pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).searchActiveProducts("laptop", pageable);
        }

        @Test
        @DisplayName("Should return featured products")
        void shouldReturnFeaturedProducts() {
            Pageable limit = Pageable.ofSize(10);
            when(productRepository.findFeaturedWithImages(limit)).thenReturn(List.of(testProduct));
            when(productMapper.toResponseList(anyList())).thenReturn(List.of(testResponse));

            List<ProductResponse> results = productService.findFeatured();

            assertThat(results).hasSize(1);
            verify(productRepository).findFeaturedWithImages(limit);
        }

        @Test
        @DisplayName("Should return low stock products")
        void shouldReturnLowStockProducts() {
            when(productRepository.findLowStockProducts()).thenReturn(List.of(testProduct));
            when(productMapper.toResponseList(anyList())).thenReturn(List.of(testResponse));

            List<ProductResponse> results = productService.findLowStock();

            assertThat(results).hasSize(1);
            verify(productRepository).findLowStockProducts();
        }

        @Test
        @DisplayName("Should return empty list when no products in category")
        void shouldReturnEmptyList_whenNoProductsInCategory() {
            when(productRepository.findByCategoryIdAndActiveTrue(1L)).thenReturn(List.of());

            List<ProductResponse> results = productService.findByCategory(1L);

            assertThat(results).isEmpty();
            verify(productRepository).findByCategoryIdAndActiveTrue(1L);
        }

        @Test
        @DisplayName("Should return empty page when no search results")
        void shouldReturnEmptyPage_whenNoSearchResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> emptyPage = new PageImpl<>(List.of());

            when(productRepository.searchActiveProducts("nonexistent", pageable)).thenReturn(emptyPage);

            Page<ProductResponse> result = productService.searchProducts("nonexistent", pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            verify(productRepository).searchActiveProducts("nonexistent", pageable);
        }
    }
}