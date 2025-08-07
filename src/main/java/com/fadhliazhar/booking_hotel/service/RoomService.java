package com.fadhliazhar.booking_hotel.service;

import com.fadhliazhar.booking_hotel.dto.room.RoomAvailabilityRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomAvailabilityResponseDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomResponseDTO;
import com.fadhliazhar.booking_hotel.exception.BusinessValidationException;
import com.fadhliazhar.booking_hotel.exception.ResourceNotFoundException;
import com.fadhliazhar.booking_hotel.mapper.RoomMapper;
import com.fadhliazhar.booking_hotel.model.BookingStatus;
import com.fadhliazhar.booking_hotel.model.Room;
import com.fadhliazhar.booking_hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public List<RoomResponseDTO> getAll() {
        return roomMapper.toResponseDTOs(roomRepository.findAll(Sort.by("roomNumber")));
    }

    public RoomResponseDTO getById(Long roomId) {
        return roomMapper.toResponseDTO(roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + roomId + " not found."))
        );
    }

    public RoomAvailabilityResponseDTO getAvailableRoom(RoomAvailabilityRequestDTO roomAvailabilityRequestDTO) {
        Optional<Room> availableRoom = roomRepository.findOneAvailableRoom(
                roomAvailabilityRequestDTO.getNumberOfAdults(),
                roomAvailabilityRequestDTO.getNumberOfChildren(),
                roomAvailabilityRequestDTO.getCheckInDate(),
                roomAvailabilityRequestDTO.getCheckOutDate(),
                BookingStatus.CANCELED
        );

        return availableRoom.map(roomMapper::toRoomAvailabilityResponseDTO).orElseThrow(() -> new ResourceNotFoundException("No available room"));
    }

    public RoomResponseDTO create(RoomRequestDTO requestedRoom) {
        boolean roomExists = roomRepository.existsByRoomNumber(requestedRoom.getRoomNumber());
        if (roomExists) {
            throw new BusinessValidationException("Room number " + requestedRoom.getRoomNumber() + " already exists.");
        }

        Room roomEntity = roomMapper.toEntity(requestedRoom);
        Room savedRoom = roomRepository.save(roomEntity);

        return roomMapper.toResponseDTO(savedRoom);
    }

    public RoomResponseDTO update(Long roomId, RoomRequestDTO requestedRoom) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + roomId + " not found."));

        room.setRoomNumber(requestedRoom.getRoomNumber());
        room.setRoomPrice(requestedRoom.getRoomPrice());
        room.setAdultCapacity(requestedRoom.getAdultCapacity());
        room.setChildrenCapacity(requestedRoom.getChildrenCapacity());

        Room savedRoom = roomRepository.save(room);

        return roomMapper.toResponseDTO(savedRoom);
    }

    public void deleteById(Long roomId) {
        boolean roomExists = roomRepository.existsById(roomId);
        if (!roomExists) {
            throw new ResourceNotFoundException("Room with ID " + roomId + " not found.");
        }

        roomRepository.deleteById(roomId);
    }
}
