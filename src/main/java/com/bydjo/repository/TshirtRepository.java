package com.bydjo.repository;

import com.bydjo.entity.Tshirt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TshirtRepository extends JpaRepository<Tshirt, Long> {
    Optional<Tshirt> findByCode(String code);
    List<Tshirt> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    boolean existsByCode(String code);
}
