package com.fadhliazhar.booking_hotel.controller;

import com.fadhliazhar.booking_hotel.dto.room.RoomAvailabilityRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomAvailabilityResponseDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomResponseDTO;
import com.fadhliazhar.booking_hotel.service.RoomService;
import com.fadhliazhar.booking_hotel.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponseDTO>>> getAllRooms() {
        List<RoomResponseDTO> rooms = roomService.getAll();
        ApiResponse<List<RoomResponseDTO>> response = ApiResponse.success("Success", rooms);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> getRoomById(@PathVariable Long id) {
        RoomResponseDTO room = roomService.getById(id);
        ApiResponse<RoomResponseDTO> response = ApiResponse.success("Success", room);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/available-room")
    public ResponseEntity<ApiResponse<RoomAvailabilityResponseDTO>> getAvailableRoom(@Valid @RequestBody RoomAvailabilityRequestDTO request) {
        RoomAvailabilityResponseDTO availableRoom = roomService.getAvailableRoom(request);
        ApiResponse<RoomAvailabilityResponseDTO> response = ApiResponse.success("Success", availableRoom);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> createRoom(@Valid @RequestBody RoomRequestDTO request) {
        RoomResponseDTO savedRoom = roomService.create(request);
        ApiResponse<RoomResponseDTO> response = ApiResponse.success(201, "Room created successfully", savedRoom);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedRoom.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequestDTO request) {
        RoomResponseDTO room = roomService.update(id, request);
        ApiResponse<RoomResponseDTO> response = ApiResponse.success("Room updated successfully", room);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteById(id);
        ApiResponse<Void> response = ApiResponse.success(200, "Room deleted successfully", null);

        return ResponseEntity.ok(response);
    }
}
