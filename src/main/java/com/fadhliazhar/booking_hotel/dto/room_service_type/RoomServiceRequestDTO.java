package com.fadhliazhar.booking_hotel.dto.room_service_type;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RoomServiceRequestDTO {
    @NotNull(message = "Service type ID is required")
    @Min(value = 1, message = "Service type ID must be at least 1")
    private Long serviceTypeId;

    @NotNull(message = "Booking ID is required")
    @Min(value = 1, message = "Booking ID must be at least 1")
    private Long bookingId;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date must be today or in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    private String notes;
}
