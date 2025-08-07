package com.fadhliazhar.booking_hotel.service;

import com.fadhliazhar.booking_hotel.dto.room_service_type.RoomServiceRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room_service_type.RoomServiceResponseDTO;
import com.fadhliazhar.booking_hotel.exception.ResourceNotFoundException;
import com.fadhliazhar.booking_hotel.mapper.RoomServiceMapper;
import com.fadhliazhar.booking_hotel.model.RoomService;
import com.fadhliazhar.booking_hotel.repository.BookingRepository;
import com.fadhliazhar.booking_hotel.repository.RoomRepository;
import com.fadhliazhar.booking_hotel.repository.RoomServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class RoomService_Service {
    private final RoomServiceRepository roomServiceRepository;
    private final BookingRepository bookingRepository;
    private final RoomServiceMapper roomServiceMapper;

    public List<RoomServiceResponseDTO> getAll() {
        return roomServiceMapper.toResponseDTOs(roomServiceRepository.findAll());
    }

    public List<RoomServiceResponseDTO> getByBookingId(Long bookingId) {
        return roomServiceMapper.toResponseDTOs(roomServiceRepository.findByBookingId(bookingId));
    }

    public RoomServiceResponseDTO getById (Long roomServiceId) {
        return roomServiceMapper.toResponseDTO(roomServiceRepository.findById(roomServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Room service with ID " + roomServiceId + " not found."))
        );
    }

    public RoomServiceResponseDTO create(RoomServiceRequestDTO requestedRoomService) {
        boolean bookingExist = bookingRepository.existsById(requestedRoomService.getBookingId());
        if (!bookingExist) {
            throw new ResourceNotFoundException("Booking with ID " + requestedRoomService.getBookingId() + " not found.");
        }

        RoomService roomServiceEntity = roomServiceMapper.toEntity(requestedRoomService);
        RoomService savedRoomService = roomServiceRepository.save(roomServiceEntity);

        return roomServiceMapper.toResponseDTO(savedRoomService);
    }

    public void deleteById(Long roomServiceId) {
        boolean roomServiceExist = roomServiceRepository.existsById(roomServiceId);
        if (!roomServiceExist) {
            throw new ResourceNotFoundException("Room service with ID" + roomServiceId + " not found.");
        }

        roomServiceRepository.deleteById(roomServiceId);
    }

    @Transactional
    public void deleteAllByBookingId(Long bookingId) {
        boolean roomServiceExists = roomServiceRepository.existsByBookingId(bookingId);
        if (!roomServiceExists) {
            throw new ResourceNotFoundException("Room service with booking ID " + bookingId + " is not exists.");
        }

        roomServiceRepository.deleteAllByBookingId(bookingId);
    }
}
