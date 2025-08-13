package com.fadhliazhar.booking_hotel.mapper;

import com.fadhliazhar.booking_hotel.dto.booking.BookingRequestDTO;
import com.fadhliazhar.booking_hotel.dto.booking.BookingResponseDTO;
import com.fadhliazhar.booking_hotel.model.Booking;
import com.fadhliazhar.booking_hotel.model.BookingStatus;
import com.fadhliazhar.booking_hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class BookingMapper {
    private final RoomRepository roomRepository;

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

        // Fetch room number based on roomId
        if (booking.getRoomId() != null) {
            try {
                Integer roomNumber = roomRepository.findRoomNumberById(booking.getRoomId());
                dto.setRoomNumber(roomNumber);
            } catch (Exception e) {
                log.warn("Failed to fetch room number for room ID {} for booking {}: {}",
                        booking.getRoomId(), booking.getId(), e.getMessage());
                dto.setRoomNumber(null);
            }
        }

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
