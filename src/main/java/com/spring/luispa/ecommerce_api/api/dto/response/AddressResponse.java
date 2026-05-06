package com.spring.luispa.ecommerce_api.api.dto.response;

import com.spring.luispa.ecommerce_api.domain.user.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Address information response")
public class AddressResponse {

    @Schema(description = "Address ID",
            example = "1")
    private Long id;

    @Schema(description = "Street name and number",
            example = "123 New Ave.")
    private String street;

    @Schema(description = "Additional address information",
            example = "5th floor")
    private String addressLine2;

    @Schema(description = "City",
            example = "Hollywood")
    private String city;

    @Schema(description = "State or province",
            example = "California")
    private String state;

    @Schema(description = "Postal/ZIP code",
            example = "06500")
    private String zipCode;

    @Schema(description = "Country",
            example = "United States")
    private String country;

    @Schema(description = "Phone number",
            example = "+10 1010 1010")
    private String phoneNumber;

    @Schema(description = "Whether this is the default address",
            example = "true")
    private boolean defaultAddress;

    @Schema(description = "Address type",
            example = "SHIPPING")
    private AddressType addressType;

    @Schema(description = "Formatted address string",
        example = "123 New Ave., 5th Floor, Hollywood, US 06500, United States")
    private String formattedAddress;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
}
