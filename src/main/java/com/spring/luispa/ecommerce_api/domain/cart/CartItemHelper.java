package com.spring.luispa.ecommerce_api.domain.cart;

public class CartItemHelper {

    public CartItemHelper() {
        // No-args constructor
    }

    public static String getFirstImageUrl(CartItem item) {
        if (item == null ||
            item.getProduct() == null ||
            item.getProduct().getImages() == null ||
            item.getProduct().getImages().isEmpty()) {
            return null;
        }

            return item.getProduct().getImages().get(0).getImageUrl();
    }
}
