package com.spring.luispa.ecommerce_api.domain.user;

import com.spring.luispa.ecommerce_api.shared.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {

    /**
     * Find a role by its name
     * @param name the role name (ROLE_USER, ROLE_ADMIN)
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(RoleName name);

    /**
     * Check if role exists by name
     * @param name the role name
     * @return true if exists, false otherwise
     */
    boolean existsByName(RoleName name);
}
