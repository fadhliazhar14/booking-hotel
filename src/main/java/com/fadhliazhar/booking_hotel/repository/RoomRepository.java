package com.fadhliazhar.booking_hotel.repository;

import com.fadhliazhar.booking_hotel.model.Room;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByRoomNumber(Integer roomNumber);
    boolean existsById(@NonNull Long roomId);

    @Query("SELECT r.roomNumber FROM Room r WHERE r.id = :roomId")
    Integer findRoomNumberById(@Param("roomId") Long roomId);

    @Query(value = """
            SELECT r.*
            FROM room r
            WHERE r.adult_capacity >= :numberOfAdults
            AND (r.adult_capacity + r.children_capacity) >= (:numberOfAdults + :numberOfChildren)
            AND NOT EXISTS (
                SELECT 1
                FROM booking b
                WHERE b.room_id = r.id
                AND b.booking_status IN ('BOOKED', 'CHECKED_IN')
                AND NOT (
                    b.checked_out_date <= :checkInDate
                    OR b.checked_in_date >= :checkOutDate
                )
            )
            ORDER BY r.room_price ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Room> findOneAvailableRoom(
            @Param("numberOfAdults") int numberOfAdults,
            @Param("numberOfChildren") int numberOfChildren,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );
}
