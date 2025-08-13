package com.fadhliazhar.booking_hotel.dto.room_amenity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomAmenityRequestDTO {
    @NotNull(message = "Amenity type ID is required")
    @Min(value = 1, message = "Amenity type ID must be at least 1")
    private Long amenityTypeId;

    @NotNull(message = "Room ID is required")
    @Min(value = 1, message = "Room ID must be at least 1")
    private Long roomId;
}
