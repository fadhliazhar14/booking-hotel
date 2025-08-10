package com.fadhliazhar.booking_hotel.controller;

import com.fadhliazhar.booking_hotel.dto.booking.BookingRequestDTO;
import com.fadhliazhar.booking_hotel.dto.booking.BookingResponseDTO;
import com.fadhliazhar.booking_hotel.dto.booking.BookingStatusUpdateDTO;
import com.fadhliazhar.booking_hotel.service.BookingService;
import com.fadhliazhar.booking_hotel.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getAllBookings() {
        List<BookingResponseDTO> bookings = bookingService.getAll();
        ApiResponse<List<BookingResponseDTO>> response = ApiResponse.success("Success", bookings);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> getBookingById(@PathVariable Long id) {
        BookingResponseDTO booking = bookingService.getById(id);
        ApiResponse<BookingResponseDTO> response = ApiResponse.success("Success", booking);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> createBooking(@Valid @RequestBody BookingRequestDTO request) {
        BookingResponseDTO savedBooking = bookingService.create(request);
        ApiResponse<BookingResponseDTO> response = ApiResponse.success(201, "Booking created successfully", savedBooking);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedBooking.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> updateBooking(@PathVariable Long id, @Valid @RequestBody BookingRequestDTO request) {
        BookingResponseDTO updatedBooking = bookingService.update(id, request);
        ApiResponse<BookingResponseDTO> response = ApiResponse.success("Booking updated successfully", updatedBooking);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> updateBookingStatus(@PathVariable Long bookingId, @RequestBody BookingStatusUpdateDTO statusUpdateDto) {
        BookingResponseDTO updatedBooking = bookingService.updateStatus(bookingId, statusUpdateDto);
        ApiResponse<BookingResponseDTO> response = ApiResponse.success("Booking status updated successfully", updatedBooking);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long id) {
        bookingService.deleteById(id);
        ApiResponse<Void> response = ApiResponse.success("Booking deleted successfully", null);

        return ResponseEntity.ok(response);
    }
}
