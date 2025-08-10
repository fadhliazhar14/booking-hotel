package com.fadhliazhar.booking_hotel.controller;

import com.fadhliazhar.booking_hotel.dto.room_amenity.RoomAmenityRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room_amenity.RoomAmenityResponseDTO;
import com.fadhliazhar.booking_hotel.service.RoomAmenityService;
import com.fadhliazhar.booking_hotel.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/room-amenities")
@RequiredArgsConstructor
public class RoomAmenityController {
    private final RoomAmenityService roomAmenityService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomAmenityResponseDTO>>> getAllRoomAmenities() {
        List<RoomAmenityResponseDTO> roomAmenities = roomAmenityService.getAll();
        ApiResponse<List<RoomAmenityResponseDTO>> response = ApiResponse.success("Success", roomAmenities);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<RoomAmenityResponseDTO>>> getRoomAmenitiesByRoomId(@PathVariable Long roomId) {
        List<RoomAmenityResponseDTO> roomAmenities = roomAmenityService.getByRoomId(roomId);
        ApiResponse<List<RoomAmenityResponseDTO>> response = ApiResponse.success("Success", roomAmenities);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomAmenityResponseDTO>> getRoomAmenityById(@PathVariable Long id) {
        RoomAmenityResponseDTO roomAmenity = roomAmenityService.getById(id);
        ApiResponse<RoomAmenityResponseDTO> response = ApiResponse.success("Success", roomAmenity);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RoomAmenityResponseDTO>> createRoomAmenity(@Valid @RequestBody RoomAmenityRequestDTO request) {
        RoomAmenityResponseDTO savedRoomAmenity = roomAmenityService.create(request);
        ApiResponse<RoomAmenityResponseDTO> response = ApiResponse.success(201, "Room amenity created successfully", savedRoomAmenity);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedRoomAmenity.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoomAmenity(@PathVariable Long id) {
        roomAmenityService.deleteById(id);
        ApiResponse<Void> response = ApiResponse.success("Room amenity deleted successfully", null);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteAllRoomAmenityByRoomId(@PathVariable Long roomId) {
        roomAmenityService.deleteAllByRoomId(roomId);
        ApiResponse<Void> response = ApiResponse.success("Room amenities with room ID " + roomId + " deleted successfully", null);

        return ResponseEntity.ok(response);
    }
}
