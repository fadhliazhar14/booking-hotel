package com.fadhliazhar.booking_hotel.dto.room_amenity;

import com.fadhliazhar.booking_hotel.model.Amenity;
import lombok.Data;

@Data
public class RoomAmenityResponseDTO {
    private Long id;
    private Amenity amenity;
    private Long roomId;
}
