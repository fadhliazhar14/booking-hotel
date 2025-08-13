package com.fadhliazhar.booking_hotel.repository;

import com.fadhliazhar.booking_hotel.model.Booking;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsById(@NonNull Long bookingId);
    
    @Query("SELECT b FROM Booking b WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(CONCAT(b.firstName, ' ', b.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "CAST(b.id AS string) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(b.room.id AS string) LIKE CONCAT('%', :search, '%') OR " +
           "LOWER(CAST(b.bookingStatus AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Booking> findAllWithSearch(@Param("search") String search, Pageable pageable);
}
