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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for BookingService
 * Tests all business scenarios including edge cases and error conditions
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("BookingService Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    private Booking testBooking;
    private Room testRoom;
    private BookingRequestDTO bookingRequestDTO;
    private BookingResponseDTO bookingResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup test room
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setRoomNumber(101);
        testRoom.setRoomPrice(new BigDecimal("150.00"));
        testRoom.setAdultCapacity(2);
        testRoom.setChildrenCapacity(1);

        // Setup test booking
        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setFirstName("John");
        testBooking.setLastName("Doe");
        testBooking.setCheckedInDate(LocalDate.now().plusDays(1));
        testBooking.setCheckedOutDate(LocalDate.now().plusDays(3));
        testBooking.setAdultCapacity(2);
        testBooking.setChildrenCapacity(0);
        testBooking.setRoom(testRoom);
        testBooking.setBookingStatus(BookingStatus.BOOKED);
        testBooking.setTotalAmount(new BigDecimal("300.00"));
        testBooking.setUserId("user123");
        testBooking.setCreatedAt(LocalDateTime.now());

        // Setup DTOs
        bookingRequestDTO = new BookingRequestDTO();
        bookingRequestDTO.setFirstName("John");
        bookingRequestDTO.setLastName("Doe");
        bookingRequestDTO.setCheckedInDate(LocalDate.now().plusDays(1));
        bookingRequestDTO.setCheckedOutDate(LocalDate.now().plusDays(3));
        bookingRequestDTO.setAdultCapacity(2);
        bookingRequestDTO.setChildrenCapacity(0);
        bookingRequestDTO.setRoomId(1L);

        bookingResponseDTO = new BookingResponseDTO();
        bookingResponseDTO.setId(1L);
        bookingResponseDTO.setFirstName("John");
        bookingResponseDTO.setLastName("Doe");
        bookingResponseDTO.setCheckedInDate(LocalDate.now().plusDays(1));
        bookingResponseDTO.setCheckedOutDate(LocalDate.now().plusDays(3));
        bookingResponseDTO.setRoomId(1L);
        bookingResponseDTO.setRoomNumber(101);
        bookingResponseDTO.setBookingStatus(BookingStatus.BOOKED);
        bookingResponseDTO.setNight(2);
    }

    @Test
    @DisplayName("Should get all bookings with pagination successfully")
    void shouldGetAllBookingsWithPagination() {
        // Given
        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPage(0);
        pageRequest.setSize(10);
        pageRequest.setSort("id");
        pageRequest.setDirection("desc");

        Page<Booking> mockPage = new PageImpl<>(List.of(testBooking), 
            Pageable.ofSize(10), 1);
        
        when(bookingRepository.findAllWithSearch(any(), any())).thenReturn(mockPage);
        when(bookingMapper.toResponseDTO(testBooking)).thenReturn(bookingResponseDTO);

        // When
        PageResponseDTO<BookingResponseDTO> result = bookingService.getAll(pageRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        
        verify(bookingRepository).findAllWithSearch(any(), any());
        verify(bookingMapper).toResponseDTO(testBooking);
    }

    @Test
    @DisplayName("Should get booking by ID successfully")
    void shouldGetBookingByIdSuccessfully() {
        // Given
        Long bookingId = 1L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(bookingMapper.toResponseDTO(testBooking)).thenReturn(bookingResponseDTO);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::isAuthenticated).thenReturn(true);
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of("user123"));
            mockedSecurityUtils.when(() -> SecurityUtils.hasRole("ADMIN")).thenReturn(false);

            // When
            BookingResponseDTO result = bookingService.getById(bookingId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(bookingId);
            assertThat(result.getFirstName()).isEqualTo("John");
        }
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when booking not found")
    void shouldThrowResourceNotFoundExceptionWhenBookingNotFound() {
        // Given
        Long bookingId = 999L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.getById(bookingId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Booking with ID " + bookingId + " not found");
    }

    @Test
    @DisplayName("Should throw BusinessValidationException when user tries to access other's booking")
    void shouldThrowBusinessValidationExceptionWhenAccessingOthersBooking() {
        // Given
        Long bookingId = 1L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::isAuthenticated).thenReturn(true);
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of("differentUser"));
            mockedSecurityUtils.when(() -> SecurityUtils.hasRole("ADMIN")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> bookingService.getById(bookingId))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Access denied");
        }
    }

    @Test
    @DisplayName("Should create booking successfully")
    void shouldCreateBookingSuccessfully() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.findOneAvailableRoom(any(), any(), any(), any()))
            .thenReturn(Optional.of(testRoom));
        when(bookingMapper.toEntity(bookingRequestDTO)).thenReturn(testBooking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toResponseDTO(testBooking)).thenReturn(bookingResponseDTO);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of("user123"));

            // When
            BookingResponseDTO result = bookingService.create(bookingRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            
            verify(bookingRepository).save(any(Booking.class));
        }
    }

    @Test
    @DisplayName("Should throw BusinessValidationException when room is not available")
    void shouldThrowBusinessValidationExceptionWhenRoomNotAvailable() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.findOneAvailableRoom(any(), any(), any(), any()))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.create(bookingRequestDTO))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("Room is not available");
    }

    @Test
    @DisplayName("Should throw BusinessValidationException for invalid date range")
    void shouldThrowBusinessValidationExceptionForInvalidDateRange() {
        // Given
        bookingRequestDTO.setCheckedInDate(LocalDate.now().plusDays(3));
        bookingRequestDTO.setCheckedOutDate(LocalDate.now().plusDays(1)); // Invalid: before check-in

        // When & Then
        assertThatThrownBy(() -> bookingService.create(bookingRequestDTO))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("Check-out date must be after check-in date");
    }

    @Test
    @DisplayName("Should throw BusinessValidationException for past check-in date")
    void shouldThrowBusinessValidationExceptionForPastCheckInDate() {
        // Given
        bookingRequestDTO.setCheckedInDate(LocalDate.now().minusDays(1)); // Past date
        bookingRequestDTO.setCheckedOutDate(LocalDate.now().plusDays(1));

        // When & Then
        assertThatThrownBy(() -> bookingService.create(bookingRequestDTO))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("Check-in date cannot be in the past");
    }

    @Test
    @DisplayName("Should update booking successfully")
    void shouldUpdateBookingSuccessfully() {
        // Given
        Long bookingId = 1L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toResponseDTO(testBooking)).thenReturn(bookingResponseDTO);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::isAuthenticated).thenReturn(true);
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of("user123"));
            mockedSecurityUtils.when(() -> SecurityUtils.hasRole("ADMIN")).thenReturn(false);

            // When
            BookingResponseDTO result = bookingService.update(bookingId, bookingRequestDTO);

            // Then
            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
        }
    }

    @Test
    @DisplayName("Should update booking status successfully")
    void shouldUpdateBookingStatusSuccessfully() {
        // Given
        Long bookingId = 1L;
        BookingStatusUpdateDTO statusUpdate = new BookingStatusUpdateDTO();
        statusUpdate.setBookingStatus(BookingStatus.CHECKED_IN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toResponseDTO(testBooking)).thenReturn(bookingResponseDTO);

        // When
        BookingResponseDTO result = bookingService.updateStatus(bookingId, statusUpdate);

        // Then
        assertThat(result).isNotNull();
        verify(bookingRepository).save(testBooking);
    }

    @Test
    @DisplayName("Should throw BusinessValidationException for invalid status transition")
    void shouldThrowBusinessValidationExceptionForInvalidStatusTransition() {
        // Given
        Long bookingId = 1L;
        testBooking.setBookingStatus(BookingStatus.CHECKED_OUT); // Terminal state
        
        BookingStatusUpdateDTO statusUpdate = new BookingStatusUpdateDTO();
        statusUpdate.setBookingStatus(BookingStatus.BOOKED); // Invalid transition

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));

        // When & Then
        assertThatThrownBy(() -> bookingService.updateStatus(bookingId, statusUpdate))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("Should delete booking successfully")
    void shouldDeleteBookingSuccessfully() {
        // Given
        Long bookingId = 1L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::isAuthenticated).thenReturn(true);
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of("user123"));
            mockedSecurityUtils.when(() -> SecurityUtils.hasRole("ADMIN")).thenReturn(false);

            // When
            bookingService.deleteById(bookingId);

            // Then
            verify(bookingRepository).deleteById(bookingId);
        }
    }

    @Test
    @DisplayName("Should throw BusinessValidationException when deleting checked-in booking")
    void shouldThrowBusinessValidationExceptionWhenDeletingCheckedInBooking() {
        // Given
        Long bookingId = 1L;
        testBooking.setBookingStatus(BookingStatus.CHECKED_IN);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::isAuthenticated).thenReturn(true);
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of("user123"));
            mockedSecurityUtils.when(() -> SecurityUtils.hasRole("ADMIN")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> bookingService.deleteById(bookingId))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Cannot delete a booking that is currently checked in");
        }
    }

    @Test
    @DisplayName("Should get all bookings (legacy method)")
    void shouldGetAllBookingsLegacyMethod() {
        // Given
        when(bookingRepository.findAll()).thenReturn(List.of(testBooking));
        when(bookingMapper.toResponseDTO(testBooking)).thenReturn(bookingResponseDTO);

        // When
        List<BookingResponseDTO> result = bookingService.getAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        
        verify(bookingRepository).findAll();
        verify(bookingMapper).toResponseDTO(testBooking);
    }
}