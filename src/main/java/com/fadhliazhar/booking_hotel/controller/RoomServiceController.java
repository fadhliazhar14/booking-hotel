package com.fadhliazhar.booking_hotel.controller;

import com.fadhliazhar.booking_hotel.dto.room_service_type.RoomServiceRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room_service_type.RoomServiceResponseDTO;
import com.fadhliazhar.booking_hotel.service.RoomService_Service;
import com.fadhliazhar.booking_hotel.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/v1/room-services")
@RequiredArgsConstructor
public class RoomServiceController {
    private final RoomService_Service roomServiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomServiceResponseDTO>>> getAllRoomAmenities() {
        List<RoomServiceResponseDTO> roomServices = roomServiceService.getAll();
        ApiResponse<List<RoomServiceResponseDTO>> response = ApiResponse.success("Success", roomServices);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<RoomServiceResponseDTO>>> getRoomAmenitiesByRoomId(@PathVariable Long bookingId) {
        List<RoomServiceResponseDTO> roomServices = roomServiceService.getByBookingId(bookingId);
        ApiResponse<List<RoomServiceResponseDTO>> response = ApiResponse.success("Success", roomServices);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomServiceResponseDTO>> getRoomServiceById(@PathVariable Long id) {
        RoomServiceResponseDTO roomAmenity = roomServiceService.getById(id);
        ApiResponse<RoomServiceResponseDTO> response = ApiResponse.success("Success", roomAmenity);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RoomServiceResponseDTO>> createRoomService(@Valid @RequestBody RoomServiceRequestDTO request) {
        RoomServiceResponseDTO savedRoomService = roomServiceService.create(request);
        ApiResponse<RoomServiceResponseDTO> response = ApiResponse.success(201, "Room service created successfully", savedRoomService);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedRoomService.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoomService(@PathVariable Long id) {
        roomServiceService.deleteById(id);
        ApiResponse<Void> response = ApiResponse.success("Room service deleted successfully", null);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<Void>> deleteAllRoomServiceByBookingId(@PathVariable Long bookingId) {
        roomServiceService.deleteAllByBookingId(bookingId);
        ApiResponse<Void> response = ApiResponse.success("Room services with booking ID " + bookingId + " deleted successfully", null);

        return ResponseEntity.ok(response);
    }
}
