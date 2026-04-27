package com.spring.luispa.ecommerce_api.domain.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (without roles)
     * @param email The user's email
     * @return Optional containing the user if exists
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists
     * @param email The user's email
     * @return true if the email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find enabled user by email (for login validation)
     * @param email The user's email
     * @return Optional containing the user if exists and is enabled
     */
    Optional<User> findByEmailAndEnabledTrue(String email);

    /**
     * Find all enabled users
     * @return A lista containing all the enabled users
     */
    List<User> findByEnabledTrue();

    /**
     * Find user by email with roles eagerly loaded
     * @param email The user's email
     * @return Optional with the user and the roles attached to it
     */
    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByEmail(String email);

    /**
     * Find user by ID with roles eagerly loaded
     * @param id The user's ID
     * @return Optional of User with the roles attached to it
     */
    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesById(Long id);

    /**
     * Find user by ID with roles and addresses attached to it
     * @param id The user's id
     * @return Optional of User with the roles and addresses attached to it
     */
    @EntityGraph(attributePaths = {"roles", "addresses"})
    Optional<User> findWithRolesAndAddressesById(Long id);

    /**
     * Return a list of all users with their roles included
     * @return List of User with their roles
     */
    //@EntityGraph(attributePaths = "roles")
    //List<User> findAllWithRoles();
    @EntityGraph(attributePaths = "roles")
    @Query("SELECT u FROM User u")
    List<User> findAllWithRoles();

    /**
     * Find user with roles and cart in a single query
     * @param email The user's email
     * @return Optional of User with roles and cart included
     */
    @Query("""
        SELECT u FROM User u
        LEFT JOIN FETCH u.roles
        LEFT JOIN FETCH u.carts
        WHERE u.email = :email   """)
    Optional<User> findByEmailWithRolesAndCarts(@Param("email") String email);

    /**
     * Find users with orders count
     * @return List of users with orders count
     */
    @Query("""
        SELECT u.id, u.email, u.firstName, u.lastName, COUNT(o) AS orderCount
        FROM User u
        LEFT JOIN u.orders o
        GROUP BY u.id""")
    List<Object[]> findUsersWithOrderCount();

    /**
     * Find users who have a specific role
     * @param roleName The role name
     * @return A list of user that has the specified role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRole(@Param("roleName")  String roleName);
}
