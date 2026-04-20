package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update user profile")
public class UpdateProfileRequest {

    @Schema(description = "User's first name",
        example = "Charles",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Schema(description = "User's new last name",
        example = "Carson",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
