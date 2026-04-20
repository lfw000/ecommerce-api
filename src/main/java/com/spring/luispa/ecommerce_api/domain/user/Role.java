package com.spring.luispa.ecommerce_api.domain.user;

import com.spring.luispa.ecommerce_api.shared.common.AuditableBaseEntity;
import com.spring.luispa.ecommerce_api.shared.enums.RoleName;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,  unique = true, length = 40, updatable = false)
    private RoleName name;

    @Size(max = 1000)
    @Column(length = 100)
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    protected Role() {
        // No-args constructor
    }

    public Role(RoleName name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    // equals() and hashCode()

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role other = (Role) o;
        return name != null && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() + (name == null ? 0 : name.hashCode());
    }
}
