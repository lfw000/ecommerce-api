package com.spring.luispa.ecommerce_api.entities;

import com.spring.luispa.ecommerce_api.enums.RoleName;
import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.*;

@Entity
@Table(name = "users")
public class User extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private Boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "seller", cascade =  CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade =  CascadeType.ALL)
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade =  CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    protected User() {
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

    public Boolean getEnabled() {
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

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    // Helper methods

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void addAddress(Address address) {
        this.addresses.add(address);
        address.setUser(this);
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
        if (o == null) return false;

        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();

        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();

        if (thisEffectiveClass != oEffectiveClass) return false;

        User user = (User) o;

        return getId() != null && Objects.equals(getId(), user.getId());
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
            if (email == null || password == null || firstName == null || lastName == null) {
                throw new IllegalArgumentException("Required fields cannot be null");
            }
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
