package com.fadhliazhar.booking_hotel.mapper;

import com.fadhliazhar.booking_hotel.dto.room_amenity.RoomAmenityRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room_amenity.RoomAmenityResponseDTO;
import com.fadhliazhar.booking_hotel.model.RoomAmenity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class RoomAmenityMapper {
    public RoomAmenityResponseDTO toResponseDTO(RoomAmenity roomAmenity) {
        RoomAmenityResponseDTO dto = new RoomAmenityResponseDTO();
        dto.setId(roomAmenity.getId());
        dto.setAmenity(roomAmenity.getAmenity());
        dto.setRoomId(roomAmenity.getRoomId());

        return dto;
    }

    public List<RoomAmenityResponseDTO> toResponseDTOs(List<RoomAmenity> roomAmenities) {
        return roomAmenities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public RoomAmenity toEntity(RoomAmenityRequestDTO requestDTO) {
        RoomAmenity roomAmenity = new RoomAmenity();
        roomAmenity.setAmenity(requestDTO.getAmenity());
        roomAmenity.setRoomId(requestDTO.getRoomId());

        return roomAmenity;
    }
}
