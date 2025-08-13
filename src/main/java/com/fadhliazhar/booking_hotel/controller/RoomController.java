package com.fadhliazhar.booking_hotel.controller;

import com.fadhliazhar.booking_hotel.dto.room.RoomAvailabilityRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomAvailabilityResponseDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomResponseDTO;
import com.fadhliazhar.booking_hotel.service.RoomService;
import com.fadhliazhar.booking_hotel.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Room Management", description = "APIs for managing hotel rooms with caching and availability checking")
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RoomController {
    private final RoomService roomService;

    @Operation(
        summary = "Get all rooms",
        description = """
            Retrieves all available rooms in the system sorted by room number.
            
            **Features:**
            - Results are cached for improved performance
            - Rooms are sorted by room number in ascending order
            - Includes room pricing and capacity information
            
            **Public Access:** This endpoint can be accessed by all authenticated users
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved all rooms",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Successful Response",
                    value = """
                    {
                      "status": 200,
                      "message": "Success",
                      "data": [
                        {
                          "id": 1,
                          "roomNumber": 101,
                          "roomPrice": 150.00,
                          "adultCapacity": 2,
                          "childrenCapacity": 1,
                          "createdOn": "2024-01-15T10:30:00.000Z",
                          "updatedOn": "2024-01-15T10:30:00.000Z"
                        },
                        {
                          "id": 2,
                          "roomNumber": 102,
                          "roomPrice": 200.00,
                          "adultCapacity": 3,
                          "childrenCapacity": 2,
                          "createdOn": "2024-01-15T10:30:00.000Z",
                          "updatedOn": "2024-01-15T10:30:00.000Z"
                        }
                      ]
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponseDTO>>> getAllRooms() {
        List<RoomResponseDTO> rooms = roomService.getAll();
        ApiResponse<List<RoomResponseDTO>> response = ApiResponse.success("Success", rooms);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get room by ID",
        description = """
            Retrieves detailed information about a specific room.
            
            **Features:**
            - Individual room results are cached
            - Includes complete room details and pricing
            - Fast lookup by room ID
            
            **Public Access:** Available to all authenticated users
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved room details",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Successful Response",
                    value = """
                    {
                      "status": 200,
                      "message": "Success",
                      "data": {
                        "id": 1,
                        "roomNumber": 101,
                        "roomPrice": 150.00,
                        "adultCapacity": 2,
                        "childrenCapacity": 1,
                        "createdOn": "2024-01-15T10:30:00.000Z",
                        "updatedOn": "2024-01-15T10:30:00.000Z"
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Room not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Room Not Found",
                    value = """
                    {
                      "timestamp": "2024-01-15T10:30:00.000Z",
                      "status": 404,
                      "error": "Not Found",
                      "message": "Room with ID 999 not found",
                      "path": "/api/v1/rooms/999"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> getRoomById(
            @Parameter(description = "Room ID", example = "1", required = true)
            @PathVariable Long id) {
        RoomResponseDTO room = roomService.getById(id);
        ApiResponse<RoomResponseDTO> response = ApiResponse.success("Success", room);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Find available room",
        description = """
            Finds an available room that matches the specified criteria and dates.
            
            **Business Logic:**
            - Checks room capacity against guest requirements
            - Verifies no conflicting bookings exist for the dates
            - Returns the most affordable available room
            - Uses optimized database query for performance
            
            **Validation:**
            - Check-in date cannot be in the past
            - Check-out date must be after check-in date
            - Adult capacity must be at least 1
            - Children capacity cannot be negative
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Available room found successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Available Room Response",
                    value = """
                    {
                      "status": 200,
                      "message": "Success",
                      "data": {
                        "id": 1,
                        "roomNumber": 101,
                        "roomPrice": 150.00,
                        "adultCapacity": 2,
                        "childrenCapacity": 1
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "No available room found for the specified criteria",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "No Room Available",
                    value = """
                    {
                      "timestamp": "2024-01-15T10:30:00.000Z",
                      "status": 404,
                      "error": "Not Found",
                      "message": "No available room found for the specified criteria",
                      "path": "/api/v1/rooms/available-room"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid date range or capacity values",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                    {
                      "timestamp": "2024-01-15T10:30:00.000Z",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Check-out date must be after check-in date",
                      "path": "/api/v1/rooms/available-room"
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/available-room")
    public ResponseEntity<ApiResponse<RoomAvailabilityResponseDTO>> getAvailableRoom(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Room availability search criteria",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RoomAvailabilityRequestDTO.class),
                    examples = @ExampleObject(
                        name = "Availability Check Request",
                        value = """
                        {
                          "numberOfAdults": 2,
                          "numberOfChildren": 1,
                          "checkInDate": "2024-01-15",
                          "checkOutDate": "2024-01-17"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody RoomAvailabilityRequestDTO request) {
        RoomAvailabilityResponseDTO availableRoom = roomService.getAvailableRoom(request);
        ApiResponse<RoomAvailabilityResponseDTO> response = ApiResponse.success("Success", availableRoom);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Create new room",
        description = """
            Creates a new room in the hotel system.
            
            **Admin Only:** This operation requires administrative privileges.
            
            **Business Rules:**
            - Room number must be unique
            - Room price must be greater than 0
            - Adult capacity must be at least 1
            - Children capacity cannot be negative
            
            **Features:**
            - Automatic timestamp generation
            - Input validation and sanitization
            - Cache invalidation after creation
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Room created successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Created Room Response",
                    value = """
                    {
                      "status": 201,
                      "message": "Room created successfully",
                      "data": {
                        "id": 1,
                        "roomNumber": 101,
                        "roomPrice": 150.00,
                        "adultCapacity": 2,
                        "childrenCapacity": 1,
                        "createdOn": "2024-01-15T10:30:00.000Z",
                        "updatedOn": "2024-01-15T10:30:00.000Z"
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation errors or room number already exists",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Room Number Exists",
                    value = """
                    {
                      "timestamp": "2024-01-15T10:30:00.000Z",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Room number 101 already exists",
                      "path": "/api/v1/rooms/create"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin privileges required"
        )
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> createRoom(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Room details for creation",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RoomRequestDTO.class),
                    examples = @ExampleObject(
                        name = "Create Room Request",
                        value = """
                        {
                          "roomNumber": 101,
                          "roomPrice": 150.00,
                          "adultCapacity": 2,
                          "childrenCapacity": 1
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody RoomRequestDTO request) {
        RoomResponseDTO savedRoom = roomService.create(request);
        ApiResponse<RoomResponseDTO> response = ApiResponse.success(201, "Room created successfully", savedRoom);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedRoom.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(
        summary = "Update existing room",
        description = """
            Updates an existing room with new details.
            
            **Admin Only:** This operation requires administrative privileges.
            
            **Business Rules:**
            - Cannot change room number to an existing one
            - All creation validation rules apply
            - Automatic updated timestamp
            
            **Features:**
            - Partial updates supported
            - Cache eviction after update
            - Input validation and sanitization
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Room updated successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Updated Room Response",
                    value = """
                    {
                      "status": 200,
                      "message": "Room updated successfully",
                      "data": {
                        "id": 1,
                        "roomNumber": 101,
                        "roomPrice": 175.00,
                        "adultCapacity": 2,
                        "childrenCapacity": 1,
                        "createdOn": "2024-01-15T10:30:00.000Z",
                        "updatedOn": "2024-01-15T14:30:00.000Z"
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation errors or room number conflict"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Room not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin privileges required"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> updateRoom(
            @Parameter(description = "Room ID", example = "1", required = true)
            @PathVariable Long id,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated room details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RoomRequestDTO.class),
                    examples = @ExampleObject(
                        name = "Update Room Request",
                        value = """
                        {
                          "roomNumber": 101,
                          "roomPrice": 175.00,
                          "adultCapacity": 2,
                          "childrenCapacity": 1
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody RoomRequestDTO request) {
        RoomResponseDTO room = roomService.update(id, request);
        ApiResponse<RoomResponseDTO> response = ApiResponse.success("Room updated successfully", room);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Delete room",
        description = """
            Deletes a room from the system with proper validation.
            
            **Admin Only:** This operation requires administrative privileges.
            
            **Business Rules:**
            - Cannot delete room with active bookings
            - Cannot delete room with associated amenities (they are removed first)
            - Soft delete with audit trail for historical data
            
            **Safety Features:**
            - Cascading deletion of room amenities
            - Booking conflict validation
            - Cache invalidation
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Room deleted successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Deleted Room Response",
                    value = """
                    {
                      "status": 200,
                      "message": "Room deleted successfully",
                      "data": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Room has active bookings and cannot be deleted",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Active Bookings Error",
                    value = """
                    {
                      "timestamp": "2024-01-15T10:30:00.000Z",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Cannot delete room with active bookings",
                      "path": "/api/v1/rooms/1"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Room not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin privileges required"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            @Parameter(description = "Room ID", example = "1", required = true)
            @PathVariable Long id) {
        roomService.deleteById(id);
        ApiResponse<Void> response = ApiResponse.success(200, "Room deleted successfully", null);

        return ResponseEntity.ok(response);
    }
}
