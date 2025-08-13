package com.fadhliazhar.booking_hotel.dto.room_service_type;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RoomServiceResponseDTO {
    private Long id;
    private Long serviceTypeId;
    private String serviceTypeName;
    private String serviceTypeDescription;
    private BigDecimal serviceTypeDefaultPrice;
    private Long bookingId;
    private LocalDate date;
    private BigDecimal amount;
    private Integer quantity;
    private String notes;
}
