package com.fadhliazhar.booking_hotel.dto.room;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomAvailabilityResponseDTO {
    private Long id;
    private Integer roomNumber;
    private BigDecimal roomPrice;
    private Integer adultCapacity;
    private Integer childrenCapacity;
}
