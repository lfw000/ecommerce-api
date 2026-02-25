package com.spring.luispa.ecommerce_api.entities;

import com.spring.luispa.ecommerce_api.enums.RoleName;
import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,  unique = true)
    private RoleName name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    protected Role() {
        // No-args constructor
    }

    private Role(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    // Hash and equals

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();

        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();

        if (thisEffectiveClass !=  oEffectiveClass) return false;

        Role role = (Role) o;

        return getId() != null && Objects.equals(getId(), role.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Builder

    public static Builder builder(RoleName name) {
        return new Builder(name);
    }

    public static class Builder {
        private final RoleName name;
        private String description;

        private Builder(RoleName name) {
            if (name == null) {
                throw new IllegalArgumentException("Role name cannot be null");
            }
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Role build() {
            return new Role(this);
        }
    }
}
