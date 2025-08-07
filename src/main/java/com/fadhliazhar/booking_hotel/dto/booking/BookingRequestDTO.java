package com.fadhliazhar.booking_hotel.dto.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequestDTO {
    @NotNull(message = "First name is required")
    @NotBlank(message = "This field cannot be empty")
    private String FirstName;

    @NotNull(message = "Last name is required")
    @NotBlank(message = "This field cannot be empty")
    private String LastName;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkedInDate;

    @NotNull(message = "Check-out date is required")
    @FutureOrPresent(message = "Check-out date must be today or in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkedOutDate;

    @NotNull(message = "Adult capacity is required")
    @Min(value = 1, message = "Adult capacity must be at least 1")
    private Integer adultCapacity;

    @NotNull(message = "Children capacity is required")
    private Integer childrenCapacity;

    @NotNull(message = "Room ID is required")
    @Min(value = 1, message = "Room ID must be at least 1")
    private Long roomId;
}
