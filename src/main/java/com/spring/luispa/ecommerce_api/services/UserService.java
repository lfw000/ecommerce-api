package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.api.dto.request.AddAddressRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.ChangePasswordRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RegisterRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateProfileRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.AddressResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.UserResponse;
import com.spring.luispa.ecommerce_api.domain.user.*;
import com.spring.luispa.ecommerce_api.mappers.AddressMapper;
import com.spring.luispa.ecommerce_api.mappers.UserMapper;
import com.spring.luispa.ecommerce_api.shared.enums.RoleName;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       AddressRepository addressRepository,
                       UserMapper userMapper,
                       AddressMapper addressMapper,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.addressRepository = addressRepository;
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
        this.passwordEncoder = passwordEncoder;
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
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered: " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));

        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getFirstName() != null &&  !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null &&  !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName());
        }

        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is Incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public UserResponse addRole(Long userId, RoleName roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        user.addRole(role);

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse removeRole(Long userId, RoleName roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        user.removeRole(role);

        return userMapper.toResponse(user);
    }

    @Transactional
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEnabled(true);
    }

    @Transactional
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEnabled(false);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressMapper.toEntity(request);
        address.setUser(user);

        if (addressRepository.countByUserId(userId) == 0) {
            address.setDefaultAddress(true);
        }

        user.addAddress(address);

        Address savedAddress = addressRepository.save(address);

        return addressMapper.toResponse(savedAddress);
    }

    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessException("Address does not belong to the user");
        }

        addressRepository.clearDefaultAddressFlag(userId);

        address.setDefaultAddress(true);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessException("Address does not belong to the user");
        }

        if (address.getDefaultAddress()) {
            addressRepository.findByUserId(userId).stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .findFirst()
                    .ifPresent(a -> {
                        address.setDefaultAddress(true);
                    });
        }

        addressRepository.delete(address);
    }
}
