package com.fadhliazhar.booking_hotel.repository;

import com.fadhliazhar.booking_hotel.model.AmenityType;
import com.fadhliazhar.booking_hotel.model.RoomAmenity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomAmenityRepository extends JpaRepository<RoomAmenity, Long> {
    List<RoomAmenity> findByRoomId(@NonNull Long roomId);
    
    boolean existsByAmenityTypeAndRoomId(AmenityType amenityType, @NonNull Long roomId);
    
    @Query("SELECT CASE WHEN COUNT(ra) > 0 THEN true ELSE false END FROM RoomAmenity ra WHERE ra.amenityType.id = :amenityTypeId AND ra.room.id = :roomId")
    boolean existsByAmenityTypeIdAndRoomId(@Param("amenityTypeId") Long amenityTypeId, @Param("roomId") Long roomId);
    
    boolean existsByRoomId(@NonNull Long roomId);
    
    void deleteAllByRoomId(@NonNull Long roomId);
    
    @Query("SELECT ra FROM RoomAmenity ra JOIN FETCH ra.amenityType WHERE ra.room.id = :roomId")
    List<RoomAmenity> findByRoomIdWithAmenityType(@Param("roomId") Long roomId);
}
