package com.fadhliazhar.booking_hotel.repository;

import com.fadhliazhar.booking_hotel.model.RoomService;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomServiceRepository extends JpaRepository<RoomService, Long> {
    List<RoomService> findByBookingId(@NonNull Long bookingId);
    boolean existsByBookingId(@NonNull Long bookingId);
    void deleteAllByBookingId(@NonNull Long bookingId);
}
