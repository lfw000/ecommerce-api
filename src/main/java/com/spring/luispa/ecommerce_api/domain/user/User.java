package com.spring.luispa.ecommerce_api.domain.user;

import com.spring.luispa.ecommerce_api.shared.common.AuditableBaseEntity;
import com.spring.luispa.ecommerce_api.domain.cart.Cart;
import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.shared.enums.RoleName;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.*;

@Entity(name = "User")
@Table(name = "users")
public class User extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotNull
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String password;

    @NotNull
    @Size(max = 50)
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotNull
    @Column(nullable = false)
    private Boolean enabled = true;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToMany(
            mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    @OrderBy("defaultAddress DESC, id ASC")
    private List<Address> addresses = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Cart cart;

    @OneToMany(mappedBy = "user")
    private Set<Order> orders = new HashSet<>();


    public User() {
        // No-args constructor
    }

    private User(Builder builder) {
        this.email = builder.email;
        this.password = builder.password;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.enabled = builder.enabled;
        builder.roles.forEach(this::addRole);
        builder.addresses.forEach(this::addAddress);
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
        if (cart != null) {
            cart.setUser(this);
        }
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    // Domain methods

    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setUser(null);
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasRole(RoleName roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    // equals() and hashCode()

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Builder

    public static Builder builder(String email, String password, String  firstName, String lastName) {
        return new Builder(email, password, firstName, lastName);
    }

    public static class Builder {

        private final String email;
        private final String password;
        private final String firstName;
        private final String lastName;
        private Boolean enabled = true;
        private final Set<Role> roles = new HashSet<>();
        private final List<Address> addresses = new ArrayList<>();

        private Builder(String email, String password, String firstName, String lastName) {
            Objects.requireNonNull(email, "Email cannot be null");
            Objects.requireNonNull(password, "Password cannot be null");
            Objects.requireNonNull(firstName, "First name cannot be null");
            Objects.requireNonNull(lastName, "Last name cannot be null");
            this.email = email;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder addRole(Role role) {
            this.roles.add(role);
            return this;
        }

        public Builder addAddress(Address address) {
            this.addresses.add(address);
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
