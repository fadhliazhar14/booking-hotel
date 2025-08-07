package com.fadhliazhar.booking_hotel.service;

import com.fadhliazhar.booking_hotel.dto.room_amenity.RoomAmenityRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room_amenity.RoomAmenityResponseDTO;
import com.fadhliazhar.booking_hotel.exception.BusinessValidationException;
import com.fadhliazhar.booking_hotel.exception.ResourceNotFoundException;
import com.fadhliazhar.booking_hotel.mapper.RoomAmenityMapper;
import com.fadhliazhar.booking_hotel.model.RoomAmenity;
import com.fadhliazhar.booking_hotel.repository.RoomAmenityRepository;
import com.fadhliazhar.booking_hotel.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RoomAmenityService {
    private final RoomAmenityRepository roomAmenityRepository;
    private final RoomRepository roomRepository;
    private final RoomAmenityMapper roomAmenityMapper;

    public List<RoomAmenityResponseDTO> getAll() {
        return roomAmenityMapper.toResponseDTOs(roomAmenityRepository.findAll());
    }

    public List<RoomAmenityResponseDTO> getByRoomId(Long roomId) {
        return roomAmenityMapper.toResponseDTOs(roomAmenityRepository.findByRoomId(roomId));
    }

    public RoomAmenityResponseDTO getById (Long roomAmenityId) {
        return roomAmenityMapper.toResponseDTO(roomAmenityRepository.findById(roomAmenityId)
                .orElseThrow(() -> new ResourceNotFoundException("Room amenity with ID " + roomAmenityId + " not found."))
        );
    }

    public RoomAmenityResponseDTO create(RoomAmenityRequestDTO requestedRoomAmenity) {
        boolean roomExists = roomRepository.existsById(requestedRoomAmenity.getRoomId());
        if (!roomExists) {
            throw new ResourceNotFoundException("Room with ID " + requestedRoomAmenity.getRoomId() + " not found.");
        }

        boolean roomAmenityExist = roomAmenityRepository.existsByAmenityAndRoomId(requestedRoomAmenity.getAmenity(), requestedRoomAmenity.getRoomId());
        if (roomAmenityExist) {
            throw new BusinessValidationException("Room amenity " + requestedRoomAmenity.getAmenity() + "with room ID " + requestedRoomAmenity.getRoomId() + " already exists.");
        }

        RoomAmenity roomAmenityEntity = roomAmenityMapper.toEntity(requestedRoomAmenity);
        RoomAmenity savedRoomAmenity = roomAmenityRepository.save(roomAmenityEntity);

        return roomAmenityMapper.toResponseDTO(savedRoomAmenity);
    }

    public void deleteById(Long roomAmenityId) {
        boolean roomAmenityExist = roomAmenityRepository.existsById(roomAmenityId);
        if (!roomAmenityExist) {
            throw new ResourceNotFoundException("Room amenity with ID" + roomAmenityId + " not found.");
        }

        roomAmenityRepository.deleteById(roomAmenityId);
    }

    @Transactional
    public void deleteAllByRoomId(Long roomId) {
        boolean roomAmenityExists = roomAmenityRepository.existsByRoomId(roomId);
        if (!roomAmenityExists) {
            throw new ResourceNotFoundException("Room amenity with room ID " + roomId + " is not exists.");
        }

        roomAmenityRepository.deleteAllByRoomId(roomId);
    }
}
