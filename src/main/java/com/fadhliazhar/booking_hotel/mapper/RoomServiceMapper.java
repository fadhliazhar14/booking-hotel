package com.fadhliazhar.booking_hotel.mapper;

import com.fadhliazhar.booking_hotel.dto.room_service_type.RoomServiceRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room_service_type.RoomServiceResponseDTO;
import com.fadhliazhar.booking_hotel.model.RoomService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class RoomServiceMapper {
    public RoomServiceResponseDTO toResponseDTO(RoomService roomService) {
        RoomServiceResponseDTO dto = new RoomServiceResponseDTO();
        dto.setId(roomService.getId());
        dto.setRoomServiceType(roomService.getRoomServiceType());
        dto.setBookingId(roomService.getBookingId());
        dto.setDate(roomService.getDate());
        dto.setAmount(roomService.getAmount());

        return dto;
    }

    public List<RoomServiceResponseDTO> toResponseDTOs(List<RoomService> roomServices) {
        return roomServices.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public RoomService toEntity(RoomServiceRequestDTO requestDTO) {
        RoomService roomService = new RoomService();
        roomService.setRoomServiceType(requestDTO.getRoomServiceType());
        roomService.setBookingId(requestDTO.getBookingId());
        roomService.setDate(requestDTO.getDate());
        roomService.setAmount(requestDTO.getAmount());

        return roomService;
    }
}