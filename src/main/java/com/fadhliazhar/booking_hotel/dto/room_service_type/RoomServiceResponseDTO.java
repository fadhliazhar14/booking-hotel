package com.fadhliazhar.booking_hotel.dto.room_service_type;

import com.fadhliazhar.booking_hotel.model.RoomServiceType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RoomServiceResponseDTO {
    private Long id;
    private RoomServiceType roomServiceType;
    private Long bookingId;
    private LocalDate date;
    private BigDecimal amount;
}
