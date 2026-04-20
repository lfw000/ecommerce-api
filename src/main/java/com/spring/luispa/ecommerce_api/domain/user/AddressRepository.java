package com.spring.luispa.ecommerce_api.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Find all address for a specific user
     * @param userId the user ID
     * @return list of addresses ordered by default first, then by creation date
     */
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.defaultAddress DESC, a.createdAt ASC")
    List<Address> findByUserId(@Param("userId") Long userId);

    /**
     * Find the default address for a user
     * @param userId the user ID
     * @return Optional containing the default address if exists
     */
    Optional<Address> findByUserIdAndDefaultAddressTrue(Long userId);


    /**
     * Check if a user has any address
     * @param userId the user ID
     * @return true if at least one address exists
     */
    boolean existsByUserId(Long userId);

    /**
     * Count address for a user
     * @param userId the user ID
     * @return number of addresses
     */
    long countByUserId(Long userId);

    /**
     * Clear the default address flag for all addresses of a user
     * Used when setting a new default address
     * @param userId the user ID
     */
    @Modifying
    @Transactional
    @Query("""
    UPDATE Address a
    SET a.defaultAddress = false
    WHERE a.user.id = :userId
    """)
    void clearDefaultAddressFlag(@Param("userId") Long userId);

    /**
     * Find address by type (SHIPPING, BILLING, BOTH)
     * @param userId the user ID
     * @param addressType the address type
     * @return list of addresses matching the type
     */
    List<Address> findByUserIdAndAddressType(Long userId, AddressType addressType);

    /**
     * Find addresses in a specific country
     * @param userId the user ID
     * @param country the country name
     * @return list of addresses
     */
    List<Address> findByUserIdAndCountry(Long userId, String country);
}
