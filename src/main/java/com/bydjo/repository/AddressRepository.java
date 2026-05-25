package com.bydjo.repository;

import com.bydjo.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserIdOrderByIsDefaultDesc(Long userId);
    Optional<Address> findByIdAndUserId(Long id, Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
    void deleteByUserId(Long userId);
}
