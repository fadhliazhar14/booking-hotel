package com.fadhliazhar.booking_hotel.dto.room;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RoomResponseDTO {
    private Long id;
    private Integer roomNumber;
    private BigDecimal roomPrice;
    private Integer adultCapacity;
    private Integer childrenCapacity;
    private LocalDateTime createdOn ;
    private LocalDateTime updatedOn;
}