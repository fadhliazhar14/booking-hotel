package com.fadhliazhar.booking_hotel.dto.room;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class RoomRequestDTO {
    @NotNull(message = "Room number is required")
    @Min(value = 1, message = "Room number must be at least 1")
    private Integer roomNumber;

    @NotNull(message = "Room price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Room price must be greater than 0")
    private BigDecimal roomPrice;

    @NotNull(message = "Adult capacity is required")
    @Min(value = 1, message = "Adult capacity must be at least 1")
    private Integer adultCapacity;

    @NotNull(message = "Children capacity is required")
    @Min(value = 0, message = "Children capacity cannot be negative")
    private Integer childrenCapacity;
}
