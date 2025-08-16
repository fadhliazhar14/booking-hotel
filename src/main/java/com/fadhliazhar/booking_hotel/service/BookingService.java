package com.fadhliazhar.booking_hotel.service;

import com.fadhliazhar.booking_hotel.dto.booking.BookingRequestDTO;
import com.fadhliazhar.booking_hotel.dto.booking.BookingResponseDTO;
import com.fadhliazhar.booking_hotel.dto.booking.BookingStatusUpdateDTO;
import com.fadhliazhar.booking_hotel.dto.common.PageRequestDTO;
import com.fadhliazhar.booking_hotel.dto.common.PageResponseDTO;
import com.fadhliazhar.booking_hotel.exception.BusinessValidationException;
import com.fadhliazhar.booking_hotel.exception.ResourceNotFoundException;
import com.fadhliazhar.booking_hotel.mapper.BookingMapper;
import com.fadhliazhar.booking_hotel.model.Booking;
import com.fadhliazhar.booking_hotel.model.BookingStatus;
import com.fadhliazhar.booking_hotel.model.Room;
import com.fadhliazhar.booking_hotel.repository.BookingRepository;
import com.fadhliazhar.booking_hotel.repository.RoomRepository;
import com.fadhliazhar.booking_hotel.security.SecurityUtils;
import static com.fadhliazhar.booking_hotel.config.CacheConfig.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import com.fadhliazhar.booking_hotel.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final BookingMapper bookingMapper;

    /**
     * Get all bookings with pagination, sorting, and search
     */
    public PageResponseDTO<BookingResponseDTO> getAll(PageRequestDTO pageRequest) {
        Pageable pageable = PageUtil.createPageable(pageRequest);
        
        Page<Booking> bookingPage = bookingRepository.findAllWithSearch(pageRequest.getSearch(), pageable);
        
        List<BookingResponseDTO> bookingResponses = bookingPage.getContent().stream()
                .map(bookingMapper::toResponseDTO)
                .toList();

        return PageUtil.createPageResponse(
            bookingResponses,
            pageable,
            bookingPage.getTotalElements()
        );
    }

    /**
     * Get all bookings (legacy method for backward compatibility)
     */
    public List<BookingResponseDTO> getAll() {
        log.warn("Using deprecated getAll() method without pagination");
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(bookingMapper::toResponseDTO)
                .toList();
    }

    /**
     * Get booking by ID
     */
    public BookingResponseDTO getById(Long id) {
        Booking booking = findBookingById(id);
        
        // Check if user has permission to view this booking
        if (SecurityUtils.isAuthenticated()) {
            String currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            if (currentUserId != null && !currentUserId.equals(booking.getUserId()) 
                && !SecurityUtils.hasRole("ADMIN")) {
                throw new BusinessValidationException("Access denied: You can only view your own bookings");
            }
        }
        
        return bookingMapper.toResponseDTO(booking);
    }

    /**
     * Create new booking
     */
    public BookingResponseDTO create(BookingRequestDTO requestDTO) {
        validateBookingRequest(requestDTO);
        
        Room room = findRoomById(requestDTO.getRoomId());
        
        // Check room availability
        if (!isRoomAvailable(room, requestDTO.getCheckedInDate(), requestDTO.getCheckedOutDate())) {
            throw new BusinessValidationException("Room is not available for the selected dates");
        }

        Booking booking = bookingMapper.toEntity(requestDTO);
        booking.setRoom(room);
        booking.setBookingStatus(BookingStatus.BOOKED);
        
        // Set user ID from security context if available
        SecurityUtils.getCurrentUserId().ifPresent(booking::setUserId);
        
        // Calculate total amount
        booking.calculateTotalAmount();
        
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Created new booking with ID: {} for user: {}", savedBooking.getId(), savedBooking.getUserId());
        
        return bookingMapper.toResponseDTO(savedBooking);
    }

    /**
     * Update existing booking
     */
    public BookingResponseDTO update(Long id, BookingRequestDTO requestDTO) {
        Booking existingBooking = findBookingById(id);
        
        // Check if user has permission to update this booking
        if (SecurityUtils.isAuthenticated()) {
            String currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            if (currentUserId != null && !currentUserId.equals(existingBooking.getUserId()) 
                && !SecurityUtils.hasRole("ADMIN")) {
                throw new BusinessValidationException("Access denied: You can only update your own bookings");
            }
        }
        
        validateBookingRequest(requestDTO);
        
        // Check if room is changing
        if (!existingBooking.getRoomId().equals(requestDTO.getRoomId())) {
            Room newRoom = findRoomById(requestDTO.getRoomId());
            if (!isRoomAvailable(newRoom, requestDTO.getCheckedInDate(), requestDTO.getCheckedOutDate())) {
                throw new BusinessValidationException("New room is not available for the selected dates");
            }
            existingBooking.setRoom(newRoom);
        }
        
        // Update booking details
        // Update booking details manually
        existingBooking.setFirstName(requestDTO.getFirstName());
        existingBooking.setLastName(requestDTO.getLastName());
        existingBooking.setCheckedInDate(requestDTO.getCheckedInDate());
        existingBooking.setCheckedOutDate(requestDTO.getCheckedOutDate());
        existingBooking.setAdultCapacity(requestDTO.getAdultCapacity());
        existingBooking.setChildrenCapacity(requestDTO.getChildrenCapacity());
        existingBooking.calculateTotalAmount();
        
        Booking updatedBooking = bookingRepository.save(existingBooking);
        log.info("Updated booking with ID: {}", updatedBooking.getId());
        
        return bookingMapper.toResponseDTO(updatedBooking);
    }

    /**
     * Update booking status
     */
    public BookingResponseDTO updateStatus(Long bookingId, BookingStatusUpdateDTO statusUpdateDto) {
        Booking booking = findBookingById(bookingId);
        
        // Validate status transition
        validateStatusTransition(booking.getBookingStatus(), statusUpdateDto.getBookingStatus());
        
        booking.setBookingStatus(statusUpdateDto.getBookingStatus());
        Booking updatedBooking = bookingRepository.save(booking);
        
        log.info("Updated booking status to {} for booking ID: {}", 
                statusUpdateDto.getBookingStatus(), bookingId);
        
        return bookingMapper.toResponseDTO(updatedBooking);
    }

    /**
     * Delete booking by ID
     */
    public void deleteById(Long id) {
        Booking booking = findBookingById(id);
        
        // Check if user has permission to delete this booking
        if (SecurityUtils.isAuthenticated()) {
            String currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            if (currentUserId != null && !currentUserId.equals(booking.getUserId()) 
                && !SecurityUtils.hasRole("ADMIN")) {
                throw new BusinessValidationException("Access denied: You can only delete your own bookings");
            }
        }
        
        // Only allow deletion if booking is not checked in
        if (booking.getBookingStatus() == BookingStatus.CHECKED_IN) {
            throw new BusinessValidationException("Cannot delete a booking that is currently checked in");
        }
        
        bookingRepository.deleteById(id);
        log.info("Deleted booking with ID: {}", id);
    }

    // Helper methods
    
    private Booking findBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + id + " not found"));
    }
    
    private Room findRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + id + " not found"));
    }
    
    private void validateBookingRequest(BookingRequestDTO requestDTO) {
        if (requestDTO.getCheckedInDate() == null || requestDTO.getCheckedOutDate() == null) {
            throw new BusinessValidationException("Check-in and check-out dates are required");
        }
        
        LocalDate checkIn = requestDTO.getCheckedInDate();
        LocalDate checkOut = requestDTO.getCheckedOutDate();
        
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new BusinessValidationException("Check-out date must be after check-in date");
        }
        
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BusinessValidationException("Check-in date cannot be in the past");
        }
    }
    
    private boolean isRoomAvailable(Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        LocalDate checkIn = checkInDate;
        LocalDate checkOut = checkOutDate;
        
        Optional<Room> availableRoom = roomRepository.findOneAvailableRoom(
            room.getAdultCapacity(),
            room.getChildrenCapacity(),
            checkIn,
            checkOut
        );
        
        return availableRoom.isPresent() && availableRoom.get().getId().equals(room.getId());
    }
    
    private void validateStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        // Define valid status transitions
        boolean isValidTransition = switch (currentStatus) {
            case BOOKED -> newStatus == BookingStatus.CHECKED_IN || newStatus == BookingStatus.CANCELED;
            case CHECKED_IN -> newStatus == BookingStatus.CHECKED_OUT;
            case CHECKED_OUT, CANCELED -> false; // These are terminal states
        };
        
        if (!isValidTransition) {
            throw new BusinessValidationException(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }
    
}