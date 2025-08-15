package com.fadhliazhar.booking_hotel.repository;

import com.fadhliazhar.booking_hotel.model.RoomService;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomServiceRepository extends JpaRepository<RoomService, Long> {
    @Query("SELECT rs FROM RoomService rs WHERE rs.booking.id = :bookingId")
    List<RoomService> findByBookingId(@NonNull Long bookingId);

    @Query("SELECT CASE WHEN COUNT(rs) > 0 THEN true ELSE false END FROM RoomService rs WHERE rs.booking.id = :bookingId")
    boolean existsByBookingId(@NonNull Long bookingId);

    @Modifying
    @Query("DELETE FROM RoomService rs WHERE rs.booking.id = :bookingId")
    void deleteAllByBookingId(@NonNull Long bookingId);
}
