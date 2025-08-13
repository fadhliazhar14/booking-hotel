package com.fadhliazhar.booking_hotel.mapper;

import com.fadhliazhar.booking_hotel.dto.room_amenity.RoomAmenityRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room_amenity.RoomAmenityResponseDTO;
import com.fadhliazhar.booking_hotel.exception.ResourceNotFoundException;
import com.fadhliazhar.booking_hotel.model.AmenityType;
import com.fadhliazhar.booking_hotel.model.RoomAmenity;
import com.fadhliazhar.booking_hotel.repository.AmenityTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class RoomAmenityMapper {
    private final AmenityTypeRepository amenityTypeRepository;

    public RoomAmenityResponseDTO toResponseDTO(RoomAmenity roomAmenity) {
        RoomAmenityResponseDTO dto = new RoomAmenityResponseDTO();
        dto.setId(roomAmenity.getId());
        dto.setRoomId(roomAmenity.getRoomId());

        if (roomAmenity.getAmenityType() != null) {
            dto.setAmenityTypeId(roomAmenity.getAmenityType().getId());
            dto.setAmenityTypeName(roomAmenity.getAmenityType().getName());
            dto.setAmenityTypeDescription(roomAmenity.getAmenityType().getDescription());
        }

        return dto;
    }

    public List<RoomAmenityResponseDTO> toResponseDTOs(List<RoomAmenity> roomAmenities) {
        return roomAmenities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public RoomAmenity toEntity(RoomAmenityRequestDTO requestDTO) {
        AmenityType amenityType = amenityTypeRepository.findById(requestDTO.getAmenityTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Amenity type with ID " + requestDTO.getAmenityTypeId() + " not found."));

        RoomAmenity roomAmenity = new RoomAmenity();
        roomAmenity.setAmenityType(amenityType);
        roomAmenity.setRoomId(requestDTO.getRoomId());

        return roomAmenity;
    }
}
