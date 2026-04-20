package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.api.dto.request.CreateProductRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateProductRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.ProductResponse;
import com.spring.luispa.ecommerce_api.domain.product.*;
import com.spring.luispa.ecommerce_api.mappers.ProductMapper;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import com.spring.luispa.ecommerce_api.shared.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    public ProductResponse findById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Product product;

        if (isAdmin) {
            product = productRepository.findWithCategoryAndImagesById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        } else {
            product = productRepository.findActiveWithCategoryAndImagesById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        }
        return productMapper.toResponse(product);
    }

    public ProductResponse findBySku(String sku) {
        boolean isAdmin = SecurityUtils.isAdmin();
        Product product;

        if (isAdmin) {
            product = productRepository.findBySku(sku)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + sku));
        } else {
            product = productRepository.findActiveBySku(sku)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + sku));
        }

        return productMapper.toResponse(product);
    }

    public Page<ProductResponse> findAll(Pageable pageable) {
        boolean isAdmin = SecurityUtils.isAdmin();

        Page<Product> products;
        if (isAdmin) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByActiveTrue(pageable);
        }

        return  products.map(productMapper::toResponse);
    }

    public List<ProductResponse> findByCategory(Long categoryId) {
        boolean isAdmin = SecurityUtils.isAdmin();

        List<Product> products;

        if (isAdmin) {
            products = productRepository.findByCategoryId(categoryId);
        } else {
            products = productRepository.findByCategoryIdAndActiveTrue(categoryId);
        }

        return productMapper.toResponseList(products);
    }

    public List<ProductResponse> findByPriceRange(BigDecimal min, BigDecimal max) {
        boolean isAdmin = SecurityUtils.isAdmin();

        List<Product> products;

        if (isAdmin) {
            products = productRepository.findByPriceBetween(min, max);
        } else {
            products = productRepository.findByPriceBetweenAndActiveTrue(min, max);
        }

        return productMapper.toResponseList(products);
    }

    public List<ProductResponse> findByPriceLessThan(BigDecimal max) {
        List<Product> products = productRepository.findByPriceLessThanAndActiveTrue(max);

        return productMapper.toResponseList(products);
    }

    public List<ProductResponse> findByPriceGreaterThan(BigDecimal min) {
        List<Product> products = productRepository.findByPriceGreaterThanAndActiveTrue(min);

        return productMapper.toResponseList(products);
    }

    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        boolean isAdmin = SecurityUtils.isAdmin();

        Page<Product> products;

        if (isAdmin) {
            products = productRepository.searchAllProducts(keyword, pageable);
        } else {
            products = productRepository.searchActiveProducts(keyword, pageable);
        }

        return products.map(productMapper::toResponse);
    }

    public List<ProductResponse> findByCategoryAndPriceRange(Long categoryId, BigDecimal min, BigDecimal max) {
        List<Product> products = productRepository.findByCategoryAndPriceRange(categoryId, min, max);

        return productMapper.toResponseList(products);
    }

    public List<ProductResponse> findFeatured() {
        Pageable limit = Pageable.ofSize(10);

        List<Product> products = productRepository.findFeaturedWithImages(limit);

        return productMapper.toResponseList(products);
    }

    public List<ProductResponse> findLowStock() {
        List<Product> products = productRepository.findLowStockProducts();

        return productMapper.toResponseList(products);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("Product already exists with SKU: " + request.getSku());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = productMapper.toEntity(request);
        product.setCategory(category);

        // Convert attributes map to ProductAttributes
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            ProductAttributes productAttributes = new ProductAttributes();
            productAttributes.putAll(request.getAttributes());
            product.setAttributes(productAttributes);
        }

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getFeatured() != null) {
            product.setFeatured(request.getFeatured());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

            product.setCategory(category);
        }

        if (request.getPrice() != null) {
            product.updatePrice(request.getPrice());
        }

        if (request.getStock() != null) {
            if (request.getStock() > product.getStock()) {
                product.increaseStock(request.getStock() - product.getStock());
            } else if (request.getStock() < product.getStock()) {
                product.decreaseStock(product.getStock() - request.getStock());
            }
        }

        return productMapper.toResponse(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setActive(false);
    }

    @Transactional
    public void updateStock(Long productId, Integer newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.updateStock(newStock);
    }

    @Transactional
    public void adjustStock(Long productId, Integer delta) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.adjust(delta);
    }

    @Transactional
    public void updateLowStockThreshold(Long productId, Integer threshold) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setLowStockThreshold(threshold);
    }
}
