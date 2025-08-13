package com.fadhliazhar.booking_hotel.repository;

import com.fadhliazhar.booking_hotel.model.AmenityType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AmenityTypeRepository extends JpaRepository<AmenityType, Long> {
    Optional<AmenityType> findByName(String name);
    
    List<AmenityType> findByIsActiveTrue(Sort sort);
    
    boolean existsByName(String name);
    
    @Query("SELECT at FROM AmenityType at WHERE at.isActive = true")
    List<AmenityType> findAllActive();
    
    @Query("SELECT at FROM AmenityType at WHERE at.name ILIKE %:name% AND at.isActive = :isActive")
    List<AmenityType> findByNameContainingAndIsActive(@Param("name") String name, @Param("isActive") Boolean isActive);
}