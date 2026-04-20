package com.spring.luispa.ecommerce_api.api.controller;

import com.spring.luispa.ecommerce_api.api.dto.request.CreateProductRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateProductRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.ProductResponse;
import com.spring.luispa.ecommerce_api.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findById(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findBySku(sku));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findFeatured());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findByCategory(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(@RequestParam String keyword,
        @PageableDefault(size = 20)  Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.searchProducts(keyword, pageable));
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ProductResponse>> getProductsByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findByPriceRange(min, max));
    }

    // Administrator endpoints

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        productService.updateStock(id, stock);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adjustStock(@PathVariable Long id, @RequestParam Integer delta) {
        productService.adjustStock(id, delta);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //@PutMapping("/{id}/stock")
    //@PreAuthorize("hasRole('ADMIN')")
    //public ResponseEntity<Void> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
    //    return null;
    //}
}
