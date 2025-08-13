package com.fadhliazhar.booking_hotel.mapper;


import com.fadhliazhar.booking_hotel.dto.room.RoomAvailabilityResponseDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomResponseDTO;
import com.fadhliazhar.booking_hotel.model.Room;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoomMapper {

    public RoomResponseDTO toResponseDTO(Room room) {
        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setRoomPrice(room.getRoomPrice());
        dto.setAdultCapacity(room.getAdultCapacity());
        dto.setChildrenCapacity(room.getChildrenCapacity());
        dto.setCreatedOn(room.getCreatedAt());
        dto.setUpdatedOn(room.getUpdatedAt());

        return dto;
    }

    public List<RoomResponseDTO> toResponseDTOs(List<Room> rooms) {
        return rooms.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Room toEntity(RoomRequestDTO requestDTO) {
        Room room = new Room();
        room.setRoomNumber(requestDTO.getRoomNumber());
        room.setRoomPrice(requestDTO.getRoomPrice());
        room.setAdultCapacity(requestDTO.getAdultCapacity());
        room.setChildrenCapacity(requestDTO.getChildrenCapacity());

        return room;
    }

    public RoomAvailabilityResponseDTO toRoomAvailabilityResponseDTO(Room availableRoom) {
        if (availableRoom == null) {
            return null;
        }

        RoomAvailabilityResponseDTO toDTO = new RoomAvailabilityResponseDTO();
        toDTO.setId(availableRoom.getId());
        toDTO.setRoomNumber(availableRoom.getRoomNumber());
        toDTO.setRoomPrice(availableRoom.getRoomPrice());
        toDTO.setAdultCapacity(availableRoom.getAdultCapacity());
        toDTO.setChildrenCapacity(availableRoom.getChildrenCapacity());

        return toDTO;
    }
}
