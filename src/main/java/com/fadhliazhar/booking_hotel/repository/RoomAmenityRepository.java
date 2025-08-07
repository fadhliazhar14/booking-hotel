package com.fadhliazhar.booking_hotel.repository;

import com.fadhliazhar.booking_hotel.model.Amenity;
import com.fadhliazhar.booking_hotel.model.RoomAmenity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomAmenityRepository extends JpaRepository<RoomAmenity, Long> {
    List<RoomAmenity> findByRoomId(@NonNull Long roomId);
    boolean existByAmenityAndRoomId(Amenity amenity, @NonNull Long roomId);
    boolean existById(@NonNull Long roomAmenityId);
}
