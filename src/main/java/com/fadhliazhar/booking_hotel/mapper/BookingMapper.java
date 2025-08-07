package com.fadhliazhar.booking_hotel.mapper;

import com.fadhliazhar.booking_hotel.dto.booking.BookingRequestDTO;
import com.fadhliazhar.booking_hotel.dto.booking.BookingResponseDTO;
import com.fadhliazhar.booking_hotel.model.Booking;
import com.fadhliazhar.booking_hotel.model.BookingStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {
    public BookingResponseDTO toResponseDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setFirstName(booking.getFirstName());
        dto.setLastName(booking.getLastName());
        dto.setCheckedInDate(booking.getCheckedInDate());
        dto.setCheckedOutDate(booking.getCheckedOutDate());
        dto.setAdultCapacity(booking.getAdultCapacity());
        dto.setChildrenCapacity(booking.getChildrenCapacity());
        dto.setNight(booking.getNight());
        dto.setRoomId(booking.getRoomId());
        dto.setBookingStatus(booking.getBookingStatus());

        return dto;
    }

    public List<BookingResponseDTO> toResponseDTOs(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Booking toEntity(BookingRequestDTO requestDTO) {
        Booking booking = new Booking();
        booking.setFirstName(requestDTO.getFirstName());
        booking.setLastName(requestDTO.getLastName());
        booking.setCheckedInDate(requestDTO.getCheckedInDate());
        booking.setCheckedOutDate(requestDTO.getCheckedOutDate());
        booking.setAdultCapacity(requestDTO.getAdultCapacity());
        booking.setChildrenCapacity(requestDTO.getChildrenCapacity());
        booking.setRoomId(requestDTO.getRoomId());
        booking.setBookingStatus(BookingStatus.BOOKED);

        return booking;
    }
}
