package com.fadhliazhar.booking_hotel.dto.room_amenity;

import com.fadhliazhar.booking_hotel.model.Amenity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomAmenityRequestDTO {
    @NotNull(message = "Amenity is required")
    private Amenity amenity;

    @NotNull(message = "Room ID is required")
    @Min(value = 1, message = "Room ID must be at least 1")
    private Long roomId;
}
