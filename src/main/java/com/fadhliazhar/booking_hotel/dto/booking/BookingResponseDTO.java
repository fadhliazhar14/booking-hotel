package com.fadhliazhar.booking_hotel.dto.booking;

import com.fadhliazhar.booking_hotel.model.BookingStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate checkedInDate;
    private LocalDate checkedOutDate;
    private Integer adultCapacity;
    private Integer childrenCapacity;
    private Integer night;
    private Long roomId;
    private Integer roomNumber;
    private BookingStatus bookingStatus;
}
