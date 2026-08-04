package com.spring.luispa.ecommerce_api.test.helpers;

import com.spring.luispa.ecommerce_api.domain.user.Address;
import com.spring.luispa.ecommerce_api.domain.user.AddressType;
import com.spring.luispa.ecommerce_api.domain.user.User;

public class AddressTestHelper {

    public static Address defaultAddress(User user) {
        return Address.builder()
                .user(user)
                .street("Av. Reforma 123")
                .addressLine2("Piso 5, Oficina 502")
                .city("Ciudad de México")
                .state("CDMX")
                .zipCode("06500")
                .country("México")
                .phoneNumber("+52 55 1234 5678")
                .addressType(AddressType.SHIPPING)
                .build();
    }

    public static Address defaultAddress(Long id, User user) {
        Address address = defaultAddress(user);
        address.setId(id);
        return address;
    }

    public static Address defaultShippingAddress(User user) {
        return defaultAddress(user);
    }

    public static Address billingAddress(User user) {
        return Address.builder()
                .user(user)
                .street("Av. Insurgentes 456")
                .addressLine2("Piso 2")
                .city("Ciudad de México")
                .state("CDMX")
                .zipCode("06100")
                .country("México")
                .phoneNumber("+52 55 8765 4321")
                .addressType(AddressType.BILLING)
                .build();
    }

    public static Address billingAddress(Long id, User user) {
        Address address = billingAddress(user);
        address.setId(id);
        return address;
    }

    public static Address addressWithDifferentUser(User user, User owner) {
        return Address.builder()
                .user(owner)  // Dirección que pertenece a otro usuario
                .street("Calle Ajena 789")
                .city("Otra Ciudad")
                .state("Otro Estado")
                .zipCode("99999")
                .country("México")
                .phoneNumber("+52 55 9999 9999")
                .addressType(AddressType.SHIPPING)
                .build();
    }

    public static Address defaultAddressNoLine2(User user) {
        return Address.builder()
                .user(user)
                .street("Av. Reforma 123")
                .city("Ciudad de México")
                .state("CDMX")
                .zipCode("06500")
                .country("México")
                .phoneNumber("+52 55 1234 5678")
                .addressType(AddressType.SHIPPING)
                .build();
    }

    // Extra methods for integration tests

    public static Address newAddress() {
        return Address.builder()
                .street("Av. Reforma 123")
                .addressLine2("Piso 5, Oficina 502")
                .city("Ciudad de México")
                .state("CDMX")
                .zipCode("06500")
                .country("México")
                .phoneNumber("+52 55 1234 5678")
                .addressType(AddressType.SHIPPING)
                .build();
    }

    public static Address newAddress(String street, String city) {
        return Address.builder()
                .street(street)
                .city(city)
                .state("CDMX")
                .zipCode("06500")
                .country("México")
                .addressType(AddressType.SHIPPING)
                .build();
    }

    public static Address newAddressWithUser(User user) {
        return Address.builder()
                .user(user)
                .street("Av. Reforma 123")
                .addressLine2("Piso 5, Oficina 502")
                .city("Ciudad de México")
                .state("CDMX")
                .zipCode("06500")
                .country("México")
                .phoneNumber("+52 55 1234 5678")
                .addressType(AddressType.SHIPPING)
                .build();
    }
}
