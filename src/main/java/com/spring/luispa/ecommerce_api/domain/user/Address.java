package com.spring.luispa.ecommerce_api.domain.user;

import com.spring.luispa.ecommerce_api.shared.common.AuditableBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity(name = "Address")
@Table(name = "addresses")
public class Address extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String street;

    @NotNull
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String addressLine2;

    @NotNull
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String city;

    @NotNull
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String state;

    @NotNull
    @Size(max = 10)
    @Column(name = "zip_code", nullable = false, length = 10)
    private String zipCode;

    @NotNull
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String country;

    @Size(max = 20)
    @Column(length = 20)
    private String phoneNumber;

    @NotNull
    @Column(name = "is_default", nullable = false)
    private Boolean defaultAddress = false;

    @Column(name = "address_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AddressType addressType = AddressType.SHIPPING;

    @Column(length = 50)
    private String deliveryInstructions;

    protected Address() {
        // Non-args constructor
    }

    private Address(Builder builder) {
        this.user = builder.user;
        this.street = builder.street;
        this.addressLine2 = builder.addressLine2;
        this.city = builder.city;
        this.state = builder.state;
        this.zipCode = builder.zipCode;
        this.country = builder.country;
        this.phoneNumber = builder.phoneNumber;
        this.defaultAddress = builder.defaultAddress;
        this.addressType = builder.addressType;

    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(Boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }

    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }


    // Domain methods

    public void markDefault() {
        defaultAddress = true;
    }

    public String getFullAddress() {
        return String.format("%s, %s, %s, %s, %s", street, city, state, zipCode, country);
    }

    public boolean isComplete() {
        return street != null && !street.trim().isEmpty()
                && city != null && !city.trim().isEmpty()
                && state != null && !state.trim().isEmpty()
                && zipCode != null && !zipCode.trim().isEmpty()
                && country != null && !country.trim().isEmpty();
    }

    // equals() and hashCode()

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;
        Address other = (Address) o;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User user;
        private String street;
        private String addressLine2;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private String phoneNumber;
        private Boolean defaultAddress = false;
        private AddressType addressType = AddressType.SHIPPING;
        private String deliveryInstructions;

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder addressLine2(String addressLine2) {
            this.addressLine2 = addressLine2;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder zipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder isDefault(Boolean defaultAddress) {
            this.defaultAddress = defaultAddress;
            return this;
        }

        public Builder addressType(AddressType addressType) {
            this.addressType = addressType;
            return this;
        }

        public Builder deliveryInstructions(String deliveryInstructions) {
            this.deliveryInstructions = deliveryInstructions;
            return this;
        }

        public Address build() {
            //if (user == null) {
            //    throw new IllegalStateException("User is required");
            //}
            if (street == null) {
                throw new IllegalStateException("Street is required");
            }
            if (city == null) {
                throw new IllegalStateException("City is required");
            }
            if (state == null) {
                throw new IllegalStateException("State is required");
            }
            if (zipCode == null) {
                throw new IllegalStateException("Zip code is required");
            }
            if (country == null) {
                throw new IllegalStateException("Country is required");
            }

            return new Address(this);
        }
    }
}
