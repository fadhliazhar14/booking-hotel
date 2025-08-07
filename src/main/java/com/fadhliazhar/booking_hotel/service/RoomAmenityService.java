package com.fadhliazhar.booking_hotel.service;

import com.fadhliazhar.booking_hotel.dto.room_amenity.RoomAmenityRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room_amenity.RoomAmenityResponseDTO;
import com.fadhliazhar.booking_hotel.exception.BusinessValidationException;
import com.fadhliazhar.booking_hotel.exception.ResourceNotFoundException;
import com.fadhliazhar.booking_hotel.mapper.RoomAmenityMapper;
import com.fadhliazhar.booking_hotel.model.RoomAmenity;
import com.fadhliazhar.booking_hotel.repository.RoomAmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RoomAmenityService {
    private final RoomAmenityRepository roomAmenityRepository;
    private final RoomAmenityMapper roomAmenityMapper;

    public List<RoomAmenityResponseDTO> getByRoomId(Long roomId) {
        return roomAmenityMapper.toResponseDTOs(roomAmenityRepository.findByRoomId(roomId));
    }

    public RoomAmenityResponseDTO getById (Long roomAmenityId) {
        return roomAmenityMapper.toResponseDTO(roomAmenityRepository.findById(roomAmenityId)
                .orElseThrow(() -> new ResourceNotFoundException("Room amenity with " + roomAmenityId + " not found."))
        );
    }

    public RoomAmenityResponseDTO create(RoomAmenityRequestDTO requestedRoomAmenity) {
        boolean roomAmenityExist = roomAmenityRepository.existByAmenityAndRoomId(requestedRoomAmenity.getAmenity(), requestedRoomAmenity.getRoomId());
        if (roomAmenityExist) {
            throw new BusinessValidationException("Room number " + requestedRoomAmenity.getAmenity() + " already exists.");
        }

        RoomAmenity roomAmenityEntity = roomAmenityMapper.toEntity(requestedRoomAmenity);
        RoomAmenity savedRoomAmenity = roomAmenityRepository.save(roomAmenityEntity);

        return roomAmenityMapper.toResponseDTO(savedRoomAmenity);
    }

    public void deleteById(Long roomAmenityId) {
        boolean roomAmenityExist = roomAmenityRepository.existById(roomAmenityId);
        if (!roomAmenityExist) {
            throw new ResourceNotFoundException("Room amenity with " + roomAmenityId + " not found.");
        }

        roomAmenityRepository.deleteById(roomAmenityId);
    }
}
