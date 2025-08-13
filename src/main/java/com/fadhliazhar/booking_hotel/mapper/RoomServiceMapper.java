package com.fadhliazhar.booking_hotel.mapper;

import com.fadhliazhar.booking_hotel.dto.room_service_type.RoomServiceRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room_service_type.RoomServiceResponseDTO;
import com.fadhliazhar.booking_hotel.exception.ResourceNotFoundException;
import com.fadhliazhar.booking_hotel.model.RoomService;
import com.fadhliazhar.booking_hotel.model.ServiceType;
import com.fadhliazhar.booking_hotel.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class RoomServiceMapper {
    private final ServiceTypeRepository serviceTypeRepository;

    public RoomServiceResponseDTO toResponseDTO(RoomService roomService) {
        RoomServiceResponseDTO dto = new RoomServiceResponseDTO();
        dto.setId(roomService.getId());
        dto.setBookingId(roomService.getBookingId());
        dto.setDate(roomService.getDate());
        dto.setAmount(roomService.getAmount());
        dto.setQuantity(roomService.getQuantity());
        dto.setNotes(roomService.getNotes());

        if (roomService.getServiceType() != null) {
            dto.setServiceTypeId(roomService.getServiceType().getId());
            dto.setServiceTypeName(roomService.getServiceType().getName());
            dto.setServiceTypeDescription(roomService.getServiceType().getDescription());
            dto.setServiceTypeDefaultPrice(roomService.getServiceType().getDefaultPrice());
        }

        return dto;
    }

    public List<RoomServiceResponseDTO> toResponseDTOs(List<RoomService> roomServices) {
        return roomServices.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public RoomService toEntity(RoomServiceRequestDTO requestDTO) {
        ServiceType serviceType = serviceTypeRepository.findById(requestDTO.getServiceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Service type with ID " + requestDTO.getServiceTypeId() + " not found."));

        RoomService roomService = new RoomService();
        roomService.setServiceType(serviceType);
        roomService.setBookingId(requestDTO.getBookingId());
        roomService.setDate(requestDTO.getDate());
        roomService.setAmount(requestDTO.getAmount());
        roomService.setQuantity(requestDTO.getQuantity());
        roomService.setNotes(requestDTO.getNotes());

        return roomService;
    }
}