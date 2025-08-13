package com.fadhliazhar.booking_hotel.dto.room_amenity;

import lombok.Data;

@Data
public class RoomAmenityResponseDTO {
    private Long id;
    private Long amenityTypeId;
    private String amenityTypeName;
    private String amenityTypeDescription;
    private Long roomId;
}
