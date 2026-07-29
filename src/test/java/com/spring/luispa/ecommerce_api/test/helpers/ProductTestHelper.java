package com.spring.luispa.ecommerce_api.test.helpers;

import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.product.ProductAttributes;
import com.spring.luispa.ecommerce_api.domain.product.ProductImage;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class ProductTestHelper {

    public static Product defaultProduct() {
        return Product.builder(
                "LAP-TEST-001",
                "Laptop Gamer",
                    new BigDecimal("1599.99"),
                    CategoryTestHelper.defaultCategory())
                .description("Laptop Gamer")
                .stock(10)
                .active(true)
                .featured(false)
                .build();
    }

    public static Product defaultProduct(Long id) {
        Product product = defaultProduct();
        product.setId(id);
        return product;
    }

    public static Product productWithSku(String sku) {
        Product product = defaultProduct();
        product.setSku(sku);
        return product;
    }

    public static Product productWithPrice(BigDecimal price) {
        Product product = defaultProduct();
        product.setPrice(price);
        return product;
    }

    public static Product productWithStock(int stock) {
        Product product = defaultProduct();
        product.setStock(stock);
        return product;
    }

    public static Product productWithLowStock() {
        Product product = defaultProduct();
        product.setStock(3);
        product.setLowStockThreshold(5);
        return product;
    }

    public static Product productWithAttributes() {
        Product product = defaultProduct();
        ProductAttributes attributes = new ProductAttributes();
        attributes.put("color", "red");
        attributes.put("talla", "M");
        product.setAttributes(attributes);
        return product;
    }

    public static Product inactiveProduct() {
        Product product = defaultProduct();
        product.setActive(false);
        return product;
    }

    public static Product productWithImages() {
        Product product = defaultProduct();
        List<ProductImage> images = new LinkedList<>();
        images.add(new ProductImage("https://example.com/image1.jpg", true, product));
        images.add(new ProductImage("https://example.com/image2.jpg", false, product));
        product.setImages(images);
        return product;
    }
}
