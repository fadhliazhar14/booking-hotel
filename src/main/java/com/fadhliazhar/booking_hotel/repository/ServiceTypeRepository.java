package com.fadhliazhar.booking_hotel.repository;

import com.fadhliazhar.booking_hotel.model.ServiceType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {
    Optional<ServiceType> findByName(String name);
    
    List<ServiceType> findByIsActiveTrue(Sort sort);
    
    boolean existsByName(String name);
    
    @Query("SELECT st FROM ServiceType st WHERE st.isActive = true")
    List<ServiceType> findAllActive();
    
    @Query("SELECT st FROM ServiceType st WHERE st.name ILIKE %:name% AND st.isActive = :isActive")
    List<ServiceType> findByNameContainingAndIsActive(@Param("name") String name, @Param("isActive") Boolean isActive);
}