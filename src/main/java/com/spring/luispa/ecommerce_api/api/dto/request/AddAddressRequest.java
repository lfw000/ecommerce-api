package com.spring.luispa.ecommerce_api.api.dto.request;

import com.spring.luispa.ecommerce_api.domain.user.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to add a new address")
public class AddAddressRequest {

    @Schema(description = "Street and number",
        example = "123 Some Street",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @NotBlank(message = "Street is required")
    @Size(max = 100)
    private String street;

    @Schema(description = "Address details (building, floor, etc.)",
        example = "5th Floor, Office 105")
    @Size(max = 100)
    private String addressLine2;

    @Schema(description = "City",
        example = "Boston",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Schema(description = "State or province",
        example = "Massachusetts",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "State is required")
    @Size(max = 50)
    private String state;

    @Schema(description = "Zip code",
        example = "06500",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Zip code is required")
    @Size(max = 10)
    private String zipCode;

    @Schema(description = "Country",
        example = "United States",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Country is required")
    @Size(max = 50)
    private String country;

    @Schema(description = "Contact phone number",
        example = "+10 101010 10101")
    @Size(max = 20)
    private String phoneNumber;

    @Schema(description = "Address type",
        example = "SHIPPING",
        allowableValues = {"SHIPPING", "BILLING", "BOTH"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Address type is required")
    private AddressType addressType;

    @Schema(description = "Special instructions for the delivery person",
        example = "Leave with the doorman")
    private String deliveryInstruction;

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

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public String getDeliveryInstruction() {
        return deliveryInstruction;
    }

    public void setDeliveryInstruction(String deliveryInstruction) {
        this.deliveryInstruction = deliveryInstruction;
    }
}
