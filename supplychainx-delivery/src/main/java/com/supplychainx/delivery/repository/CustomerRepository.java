package com.supplychainx.delivery.repository;

import com.supplychainx.delivery.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Customer> searchCustomers(@Param("keyword") String keyword, Pageable pageable);

    Page<Customer> findByCity(String city, Pageable pageable);

    Page<Customer> findByCountry(String country, Pageable pageable);
}
