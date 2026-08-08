package com.spring.luispa.ecommerce_api.services.validation;

import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.product.ProductRepository;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.domain.user.UserRepository;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.ProductNotActiveException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CartValidator {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartValidator(UserRepository userRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public Product validateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.getActive()) {
            throw new ProductNotActiveException(product.getId(), product.getSku());
        }

        return product;
    }

    public void validateStock(Product product, Integer requestedQuantity) {
        if (requestedQuantity > product.getStock()) {
            throw new BusinessRuleException(
                    String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                            product.getSku(), product.getStock(), requestedQuantity),
                    "INSUFFICIENT_STOCK"
            );
        }
    }

    public void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessRuleException("Quantity must be positive", "INVALID_QUANTITY");
        }
    }
}
