package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.api.dto.request.CreateProductRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateProductRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.ProductResponse;
import com.spring.luispa.ecommerce_api.domain.product.*;
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
import com.spring.luispa.ecommerce_api.mappers.ProductMapper;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.DuplicateResourceException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import com.spring.luispa.ecommerce_api.shared.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final LoggingAspect loggingAspect;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductMapper productMapper, LoggingAspect loggingAspect) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
        this.loggingAspect = loggingAspect;
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
        log.info("Creating product: sku={}, name={}, categoryId={}, price={}",
                request.getSku(), request.getName(), request.getCategoryId(), request.getPrice());

        if (productRepository.existsBySku(request.getSku())) {
            log.warn("Product already exists with SKU: {}", request.getSku());
            throw new DuplicateResourceException("Product already exists with SKU: " + request.getSku());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Category not found for product creation: categoryId={}", request.getCategoryId());
                    return new ResourceNotFoundException("Category not found with id: " + request.getCategoryId());
                });

        Product product = productMapper.toEntity(request);
        product.setCategory(category);

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            log.debug("Product attributes: {}", request.getAttributes());
            ProductAttributes productAttributes = new ProductAttributes();
            productAttributes.putAll(request.getAttributes());
            product.setAttributes(productAttributes);
        }

        Product savedProduct = productRepository.save(product);

        log.info("Product created: productId={}, sku={}, name={}, category={}",
                savedProduct.getId(), savedProduct.getSku(), savedProduct.getName(),
                savedProduct.getCategory().getName());

        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product: productId={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found for update: productId={}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });

        String oldName = product.getName();
        BigDecimal oldPrice = product.getPrice();
        Integer oldStock = product.getStock();
        Boolean oldActive = product.getActive();
        Boolean oldFeatured = product.isFeatured();

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
                    .orElseThrow(() -> {
                        log.warn("Category not found for product update: categoryId={}", request.getCategoryId());
                        return new ResourceNotFoundException("Category not found");
                    });

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

        log.info("Product updated: productId={}, name: {}->{}, price: {}->{}, stock: {}->{}, active: {}->{}, " +
                "featured: {}->{}", id, oldName, product.getName(), oldPrice, product.getPrice(), oldStock,
                product.getStock(), oldActive, product.getActive(), oldFeatured, product.isFeatured());

        return productMapper.toResponse(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product: productId={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found for deletion: productId={}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });

        String sku =  product.getSku();
        String name =  product.getName();

        product.setActive(false);

        log.info("Product deleted (soft): productId={}, sku={}, name={}", id, sku, name);
    }

    @Transactional
    public void updateStock(Long productId, Integer newStock) {
        log.info("Updating stock: productId={}, newStock={}", productId, newStock);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found for stock update: productId={}", productId);
                    return new ResourceNotFoundException("Product not found");
                });

        Integer oldStock = product.getStock();

        product.updateStock(newStock);

        log.info("Stock updated: productId={}, sku={}, oldStock={}, newStock={}", productId, product.getSku(),
                oldStock, newStock);

        if (product.isLowStock()) {
            log.warn("Product is low on stock: productId={}, sku={}, stock={}, threshold={}",
                    productId, product.getSku(), oldStock, newStock);
        }
    }

    @Transactional
    public void adjustStock(Long productId, Integer delta) {
        log.info("Adjusting stock: productId={}, delta={}", productId, delta);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found for stock adjustment: productId={}", productId);
                    return new ResourceNotFoundException("Product not found");
                });

        Integer oldStock = product.getStock();
        product.adjust(delta);
        Integer newStock = product.getStock();

        log.info("Stock adjusted: productId={}, sku={}, delta={}, oldStock={}, newStock={}", productId,
                product.getSku(), delta, oldStock, newStock);

        product.adjust(delta);
    }

    @Transactional
    public void updateLowStockThreshold(Long productId, Integer threshold) {
        log.info("Updating low stock threshold: productId={}, threshold={}", productId, threshold);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found for threshold update: productId={}", productId);
                    return new ResourceNotFoundException("Product not found");
                });

        Integer oldThreshold = product.getLowStockThreshold();

        product.setLowStockThreshold(threshold);

        log.info("Low stock threshold updated: productId={}, sku={}, oldThreshold={}, newThreshold={}", productId,
                product.getSku(), oldThreshold, threshold);

        if (product.isLowStock()) {
            log.warn("Product is now considered low stock with new threshold: productId={}, sku={}, stock={}, threshold={}",
                    productId, product.getSku(), product.getStock(), threshold);
        }
    }
}
