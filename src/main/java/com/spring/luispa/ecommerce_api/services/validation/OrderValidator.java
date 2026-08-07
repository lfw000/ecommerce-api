package com.spring.luispa.ecommerce_api.services.validation;

import com.spring.luispa.ecommerce_api.domain.cart.Cart;
import com.spring.luispa.ecommerce_api.domain.cart.CartItem;
import com.spring.luispa.ecommerce_api.domain.cart.CartRepository;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.user.Address;
import com.spring.luispa.ecommerce_api.domain.user.AddressRepository;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.domain.user.UserRepository;
import com.spring.luispa.ecommerce_api.services.CartService;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.InsufficientStockException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class OrderValidator {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final CartService cartService;


    public OrderValidator(UserRepository userRepository, CartRepository cartRepository,
                          AddressRepository addressRepository, CartService cartService) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.cartService = cartService;
    }

    public User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public Cart validateCart(Long userId) {
        Cart cart = cartService.getActiveCartForCheckout(userId);

        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Cannot create order from empty cart");
        }

        return cart;
    }

    public void validateStock(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.hasStock(item.getQuantity())) {
                throw new InsufficientStockException(
                        product.getId(), product.getSku(), item.getQuantity(), product.getStock());
            }
        }
    }

    public Address validateAddress(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessRuleException("Address does not belong to the specified user");
        }

        return address;
    }
}
