package com.fadhliazhar.booking_hotel.controller;

import com.fadhliazhar.booking_hotel.dto.booking.BookingRequestDTO;
import com.fadhliazhar.booking_hotel.dto.booking.BookingResponseDTO;
import com.fadhliazhar.booking_hotel.dto.booking.BookingStatusUpdateDTO;
import com.fadhliazhar.booking_hotel.dto.common.PageRequestDTO;
import com.fadhliazhar.booking_hotel.dto.common.PageResponseDTO;
import com.fadhliazhar.booking_hotel.service.BookingService;
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

@Tag(name = "Booking Management", description = "APIs for managing hotel bookings with OAuth2 security and comprehensive business rules")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BookingController {
    private final BookingService bookingService;

    @Operation(
        summary = "Get all bookings with pagination, sorting and search",
        description = """
            Retrieves paginated list of bookings with advanced filtering capabilities.
            
            **Access Control:**
            - Regular users can only see their own bookings
            - Admins can see all bookings
            
            **Caching:** Results are cached for improved performance
            
            **Search:** Searches across firstName, lastName, and room number
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved bookings",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PageResponseDTO.class),
                examples = @ExampleObject(
                    name = "Successful Response",
                    value = """
                    {
                      "status": 200,
                      "message": "Success",
                      "data": {
                        "content": [
                          {
                            "id": 1,
                            "firstName": "John",
                            "lastName": "Doe",
                            "checkedInDate": "2024-01-15",
                            "checkedOutDate": "2024-01-17",
                            "adultCapacity": 2,
                            "childrenCapacity": 0,
                            "night": 2,
                            "roomId": 101,
                            "roomNumber": 101,
                            "bookingStatus": "BOOKED"
                          }
                        ],
                        "totalElements": 1,
                        "totalPages": 1,
                        "currentPage": 0,
                        "pageSize": 20,
                        "hasNext": false,
                        "hasPrevious": false
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Access denied"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<BookingResponseDTO>>> getAllBookings(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size (max 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Sort field", example = "id", schema = @Schema(allowableValues = {"id", "firstName", "lastName", "checkedInDate", "roomNumber"}))
            @RequestParam(defaultValue = "id") String sort,
            
            @Parameter(description = "Sort direction", example = "desc", schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "desc") String direction,
            
            @Parameter(description = "Search term for filtering across firstName, lastName, and room number", example = "John")
            @RequestParam(required = false) String search) {
        
        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPage(page);
        pageRequest.setSize(size);
        pageRequest.setSort(sort);
        pageRequest.setDirection(direction);
        pageRequest.setSearch(search);
        
        PageResponseDTO<BookingResponseDTO> bookings = bookingService.getAll(pageRequest);
        ApiResponse<PageResponseDTO<BookingResponseDTO>> response = ApiResponse.success("Success", bookings);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get all bookings (Legacy endpoint)",
        description = "Legacy endpoint without pagination. Use /api/v1/bookings instead.",
        deprecated = true
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved all bookings"
        )
    })
    @GetMapping("/all")
    @Deprecated
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getAllBookingsLegacy() {
        List<BookingResponseDTO> bookings = bookingService.getAll();
        ApiResponse<List<BookingResponseDTO>> response = ApiResponse.success("Success", bookings);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get booking by ID",
        description = """
            Retrieves a specific booking by its ID.
            
            **Access Control:**
            - Regular users can only access their own bookings
            - Admins can access any booking
            
            **Caching:** Individual booking results are cached
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved booking",
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
                        "firstName": "John",
                        "lastName": "Doe",
                        "checkedInDate": "2024-01-15",
                        "checkedOutDate": "2024-01-17",
                        "adultCapacity": 2,
                        "childrenCapacity": 0,
                        "night": 2,
                        "roomId": 101,
                        "roomNumber": 101,
                        "bookingStatus": "BOOKED"
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Booking not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Cannot access another user's booking"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> getBookingById(
            @Parameter(description = "Booking ID", example = "1", required = true)
            @PathVariable Long id) {
        BookingResponseDTO booking = bookingService.getById(id);
        ApiResponse<BookingResponseDTO> response = ApiResponse.success("Success", booking);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Create new booking",
        description = """
            Creates a new hotel booking with comprehensive validation.
            
            **Business Rules:**
            - Check-in date must be in the future
            - Check-out date must be after check-in date
            - Room must be available for the requested dates
            - Adult capacity must be at least 1
            - Children capacity cannot be negative
            - Room capacity must accommodate all guests
            
            **Conflict Prevention:** 
            System prevents overlapping bookings for the same room automatically.
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Booking created successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Created Response",
                    value = """
                    {
                      "status": 201,
                      "message": "Booking created successfully",
                      "data": {
                        "id": 1,
                        "firstName": "John",
                        "lastName": "Doe",
                        "checkedInDate": "2024-01-15",
                        "checkedOutDate": "2024-01-17",
                        "adultCapacity": 2,
                        "childrenCapacity": 0,
                        "night": 2,
                        "roomId": 101,
                        "roomNumber": 101,
                        "bookingStatus": "BOOKED"
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation errors or business rule violations",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                    {
                      "timestamp": "2024-01-15T10:30:00.000Z",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Room is not available for the selected dates",
                      "path": "/api/v1/bookings/create"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Room not found"
        )
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> createBooking(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Booking details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BookingRequestDTO.class),
                    examples = @ExampleObject(
                        name = "Create Booking Request",
                        value = """
                        {
                          "firstName": "John",
                          "lastName": "Doe",
                          "checkedInDate": "2024-01-15",
                          "checkedOutDate": "2024-01-17",
                          "adultCapacity": 2,
                          "childrenCapacity": 0,
                          "roomId": 101
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody BookingRequestDTO request) {
        BookingResponseDTO savedBooking = bookingService.create(request);
        ApiResponse<BookingResponseDTO> response = ApiResponse.success(201, "Booking created successfully", savedBooking);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedBooking.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(
        summary = "Update existing booking",
        description = """
            Updates an existing booking with the same validation rules as creation.
            
            **Access Control:**
            - Regular users can only update their own bookings
            - Admins can update any booking
            
            **Business Rules:**
            - Cannot update booking that is already checked-in or checked-out
            - All creation validation rules apply
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Booking updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Cannot update booking in current status"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Booking not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> updateBooking(
            @Parameter(description = "Booking ID", example = "1", required = true)
            @PathVariable Long id,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated booking details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BookingRequestDTO.class)
                )
            )
            @Valid @RequestBody BookingRequestDTO request) {
        BookingResponseDTO updatedBooking = bookingService.update(id, request);
        ApiResponse<BookingResponseDTO> response = ApiResponse.success("Booking updated successfully", updatedBooking);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update booking status",
        description = """
            Updates the status of a booking following business workflow:
            
            **Valid Status Transitions:**
            - BOOKED → CHECKED_IN
            - CHECKED_IN → CHECKED_OUT
            - BOOKED → CANCELLED (admin only)
            
            **Invalid Transitions:**
            - Cannot transition from CHECKED_OUT or CANCELLED
            - Regular users cannot cancel bookings
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Booking status updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid status transition"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Booking not found"
        )
    })
    @PatchMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> updateBookingStatus(
            @Parameter(description = "Booking ID", example = "1", required = true)
            @PathVariable Long bookingId,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Status update details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BookingStatusUpdateDTO.class),
                    examples = @ExampleObject(
                        name = "Check-in Request",
                        value = """
                        {
                          "bookingStatus": "CHECKED_IN"
                        }
                        """
                    )
                )
            )
            @RequestBody BookingStatusUpdateDTO statusUpdateDto) {
        BookingResponseDTO updatedBooking = bookingService.updateStatus(bookingId, statusUpdateDto);
        ApiResponse<BookingResponseDTO> response = ApiResponse.success("Booking status updated successfully", updatedBooking);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Delete booking",
        description = """
            Deletes a booking with proper business rule validation.
            
            **Access Control:**
            - Regular users can only delete their own bookings
            - Admins can delete any booking
            
            **Business Rules:**
            - Cannot delete booking that is currently checked-in
            - Checked-out bookings can be deleted (soft delete for audit trail)
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Booking deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Cannot delete booking in current status"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Booking not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(
            @Parameter(description = "Booking ID", example = "1", required = true)
            @PathVariable Long id) {
        bookingService.deleteById(id);
        ApiResponse<Void> response = ApiResponse.success("Booking deleted successfully", null);

        return ResponseEntity.ok(response);
    }
}
