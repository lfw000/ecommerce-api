package com.spring.luispa.ecommerce_api.test.helpers;

import com.spring.luispa.ecommerce_api.domain.user.User;

public class UserTestHelper {

    public static User defaultUser() {
        return User.builder(
                "test@example.com",
                "password123",
                "Test",
                "User")
                .enabled(true)
                .build();
    }

    public static User defaultUser(Long id) {
        User user = defaultUser();
        user.setId(id);
        return user;
    }

    public static User userWithEmail(String email) {
        return User.builder(
                email,
        "password123",
        "Test",
        "User")
                .enabled(true)
                .build();
    }

    public static User userWithName(String firstName, String lastName) {
        return User.builder("test@example.com", "password123", firstName, lastName)
                .enabled(true)
                .build();
    }

    public static User disabledUser() {
        return User.builder("disabled@example.com", "password123", "Disabled", "User")
                .enabled(false)
                .build();
    }

    public static User userWithId(Long id, String email) {
        User user = User.builder(email, "password123", "Test", "User")
                .enabled(true)
                .build();
        user.setId(id);
        return user;
    }

}
