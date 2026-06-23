package com.spring.luispa.ecommerce_api.api.controller;

import com.spring.luispa.ecommerce_api.api.dto.request.AddAddressRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.ChangePasswordRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateProfileRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.AddressResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.UserResponse;
import com.spring.luispa.ecommerce_api.security.CurrentUser;
import com.spring.luispa.ecommerce_api.security.UserDetailsImpl;
import com.spring.luispa.ecommerce_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile and address management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile",
            description = "Returns the profile information of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<UserResponse> getCurrentUser(
            @CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findByEmail(currentUser.getEmail()));
     }

    @PutMapping("/me")
    @Operation(summary = "Update user profile",
            description = "Updates the first and last name of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<UserResponse> updateProfile(@CurrentUser UserDetailsImpl currentUser,
                                                      @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateProfile(currentUser.getId(), request));
     }

    @PostMapping("/me/change-password")
    @Operation(summary = "Change password",
            description = "Change the password of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Current password incorrect"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Void> changePassword(@CurrentUser UserDetailsImpl currentUser,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.OK).build();
     }

    @GetMapping("/me/addresses")
    @Operation(summary = "Get user addresses",
            description = "Return all addresses of the authenticated user")
    @ApiResponse(responseCode = "200", description = "List of addresses")
    public ResponseEntity<List<AddressResponse>> getAddress(@CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserAddresses(currentUser.getId()));
     }

    @GetMapping("/me/addresses/default")
    @Operation(summary = "Get primary address",
            description = "Return the default address of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Primary address found"),
            @ApiResponse(responseCode = "404", description = "No primary address")
    })
    public ResponseEntity<AddressResponse> getDefaultAddress(@CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getDefaultAddress(currentUser.getId()));
     }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add a new address",
            description = "Adds a new address to the user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Address successfully added"),
            @ApiResponse(responseCode = "400", description = "Invalid address data")
    })
    public ResponseEntity<AddressResponse> addAddress(@CurrentUser UserDetailsImpl currentUser,
                                                      @Valid @RequestBody AddAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addAddress(currentUser.getId(), request));
     }

    @PutMapping("/me/addresses/{addressId}/default")
    @Operation(summary = "Set address as primary",
            description = "Marks an address as the default for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Primary address set"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<Void> setDefaultAddress(@CurrentUser UserDetailsImpl currentUser,
                                                  @Parameter(description = "Address ID", example = "1")
                                                  @PathVariable Long addressId) {
        userService.setDefaultAddress(currentUser.getId(), addressId);

        return ResponseEntity.status(HttpStatus.OK).build();
     }

    @DeleteMapping("/me/addresses/{addressId}")
    @Operation(summary = "Delete address",
            description = "Removes an address from the user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address deleted"),
            @ApiResponse(responseCode = "404", description = "Address not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete the only address")
    })
    public ResponseEntity<Void> deleteAddress(@CurrentUser UserDetailsImpl currentUser,
                                              @Parameter(description = "Address ID", example = "1")
                                              @PathVariable Long addressId) {
        userService.deleteAddress(currentUser.getId(), addressId);

        return ResponseEntity.status(HttpStatus.OK).build();
     }

     // Administrator endpoints

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users (admin)",
            description = "Retrieves all users in the system. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "List of users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (admin)",
            description = "Returns a specific user. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findById(id));
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable user (admin)",
            description = "Enables a user's account. Requires the ADMIN role.")
    public ResponseEntity<Void> enableUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id) {
        userService.enableUser(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disable user (admin)", description = "Deactivates a user's account. Requires the ADMIN role.")
    public ResponseEntity<Void> disableUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id) {
        userService.disableUser(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
