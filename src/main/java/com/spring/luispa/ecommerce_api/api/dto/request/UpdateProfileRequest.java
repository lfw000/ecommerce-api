package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update user profile")
public class UpdateProfileRequest {

    @Schema(description = "New first name",
        example = "Charles",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 50)
    private String firstName;

    @Schema(description = "New last name",
        example = "Smith",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 50)
    private String lastName;

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
