package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.api.dto.request.AddAddressRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.ChangePasswordRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RegisterRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateProfileRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.AddressResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.UserResponse;
import com.spring.luispa.ecommerce_api.domain.user.*;
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
import com.spring.luispa.ecommerce_api.mappers.AddressMapper;
import com.spring.luispa.ecommerce_api.mappers.UserMapper;
import com.spring.luispa.ecommerce_api.shared.enums.RoleName;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.DuplicateResourceException;
import com.spring.luispa.ecommerce_api.shared.exception.MissingDefaultRoleException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoggingAspect loggingAspect;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       AddressRepository addressRepository,
                       UserMapper userMapper,
                       AddressMapper addressMapper,
                       PasswordEncoder passwordEncoder, LoggingAspect loggingAspect) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.addressRepository = addressRepository;
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
        this.passwordEncoder = passwordEncoder;
        this.loggingAspect = loggingAspect;
    }

    public UserResponse findById(Long id){
        User user =  userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return userMapper.toResponse(user);
    }

    public UserResponse findByEmail(String email){
        User user =  userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return userMapper.toResponse(user);
    }

    User findUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    User findUserEntityWithRoles(Long id) {
        return userRepository.findWithRolesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User findByEmailWithRoles(String email) {
        return userRepository.findWithRolesByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public List<UserResponse> findAll(){
        List<User> users =  userRepository.findAll();

        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public List<User> findAllWithRoles(){
        return userRepository.findAllWithRoles();
    }

    @Transactional
    public UserResponse register(RegisterRequest request){
        log.info("Registration attempt: email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}",  request.getEmail());
            throw new DuplicateResourceException("Email already registered");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> {
                    log.error("Default role ROLE_USER not configured");
                    return new MissingDefaultRoleException("Default role not configured");
                });

        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        log.info("Registration successful: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);

        loggingAspect.setUserIdInMDC(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for profile update: userId={}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        String oldFirstName = user.getFirstName();
        String oldLastName = user.getLastName();

        if (request.getFirstName() != null &&  !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null &&  !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName());
        }

        log.info("Profile updated: userId={}, oldName={} {}, newName={} {}",
                userId, oldFirstName, oldLastName, request.getFirstName(), request.getLastName());

        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);
        loggingAspect.setUserIdInMDC(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for password change: userId={}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed - incorrect current password: userId={}", userId);
            throw new BusinessRuleException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        log.info("Password changed successfully: userId={}", userId);
    }

    @Transactional
    public UserResponse addRole(Long userId, RoleName roleName) {
        log.info("Adding role to user: userId={}, role={}", userId, roleName);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for role addition: userId={}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.warn("Role not found: roleName={}", roleName);
                    return new ResourceNotFoundException("Role not found: " + roleName);
                });

        user.addRole(role);

        log.info("Role added: userId={}, role={}",  userId, role);

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse removeRole(Long userId, RoleName roleName) {
        log.info("Removing role from user: userId={}, role={}", userId, roleName);

        loggingAspect.setUserIdInMDC(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for role removal : userId={}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.warn("Role not found: {}", roleName);
                    return new ResourceNotFoundException("Role not found: " + roleName);
                });

        user.removeRole(role);

        log.info("Role removed: userId={}, role={}", userId, roleName);

        return userMapper.toResponse(user);
    }

    @Transactional
    public void enableUser(Long userId) {
        log.info("Enabling user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for enabling: userId={}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        user.setEnabled(true);
        log.info("User enabled: {}",  userId);
    }

    @Transactional
    public void disableUser(Long userId) {
        log.info("Disabling user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for disabling: userId={}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        user.setEnabled(false);
        log.info("User disabled: {}",  userId);
    }

    public List<AddressResponse> getUserAddresses(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);

        return addressMapper.toResponseList(addresses);
    }

    public AddressResponse getDefaultAddress(Long userId) {
        Address address = addressRepository.findByUserIdAndDefaultAddressTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No default address found"));

        return addressMapper.toResponse(address);
    }

    @Transactional
    public AddressResponse addAddress(Long userId, AddAddressRequest request) {
        log.info("Adding address for user: {}", userId);
        loggingAspect.setUserIdInMDC(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for address addition: userId={}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        Address address = addressMapper.toEntity(request);
        address.setUser(user);

        if (addressRepository.countByUserId(userId) == 0) {
            address.setDefaultAddress(true);
            log.debug("First address for user {} - set as default", userId);
        }

        user.addAddress(address);

        Address savedAddress = addressRepository.save(address);

        log.info("Address added: addressId={}, userId={}", savedAddress.getId(), userId);

        return addressMapper.toResponse(savedAddress);
    }

    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        log.info("Setting default address: userId={}, addressId={}", userId, addressId);
        loggingAspect.setUserIdInMDC(userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("Address not found for default: addressId={}", addressId);
                    return new ResourceNotFoundException("Address not found");
                });

        if (!address.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to set default address {} belonging to user {}",
                    userId, addressId, address.getUser().getId());
            throw new BusinessRuleException("Address does not belong to the user");
        }

        addressRepository.clearDefaultAddressFlag(userId);

        address.setDefaultAddress(true);

        log.info("Default address set: userId={}, addressId={}", userId, addressId);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        log.info("Deleting address: userId={}, addressId={}", userId, addressId);
        loggingAspect.setUserIdInMDC(userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("Address not found for deletion: addressId={}", addressId);
                    return new ResourceNotFoundException("Address not found");
                });

        if (!address.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to delete address {} belonging to user {}",
                    userId, addressId, address.getUser().getId());
            throw new BusinessRuleException("Address does not belong to the user");
        }

        if (address.getDefaultAddress()) {
            log.debug("Deleting default address for user: {}", userId);
            addressRepository.findByUserId(userId).stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .findFirst()
                    .ifPresent(a -> {
                        address.setDefaultAddress(true);
                        log.debug("New default address set: {}", a.getId());
                    });
        }

        addressRepository.delete(address);

        log.info("Address deleted: addressId={}, userId={}", addressId, userId);
    }
}
