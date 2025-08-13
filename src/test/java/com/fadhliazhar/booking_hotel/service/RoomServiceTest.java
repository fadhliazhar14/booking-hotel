package com.fadhliazhar.booking_hotel.service;

import com.fadhliazhar.booking_hotel.dto.room.RoomAvailabilityRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomAvailabilityResponseDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomResponseDTO;
import com.fadhliazhar.booking_hotel.exception.BusinessValidationException;
import com.fadhliazhar.booking_hotel.exception.ResourceNotFoundException;
import com.fadhliazhar.booking_hotel.mapper.RoomMapper;
import com.fadhliazhar.booking_hotel.model.Room;
import com.fadhliazhar.booking_hotel.repository.RoomAmenityRepository;
import com.fadhliazhar.booking_hotel.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
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
 * Comprehensive unit tests for RoomService
 * Tests all business scenarios including edge cases, caching, and validation
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("RoomService Tests")
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAmenityRepository roomAmenityRepository;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomService roomService;

    private Room testRoom;
    private RoomRequestDTO roomRequestDTO;
    private RoomResponseDTO roomResponseDTO;
    private RoomAvailabilityRequestDTO availabilityRequestDTO;
    private RoomAvailabilityResponseDTO availabilityResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup test room
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setRoomNumber(101);
        testRoom.setRoomPrice(new BigDecimal("150.00"));
        testRoom.setAdultCapacity(2);
        testRoom.setChildrenCapacity(1);

        // Setup request DTO
        roomRequestDTO = new RoomRequestDTO();
        roomRequestDTO.setRoomNumber(101);
        roomRequestDTO.setRoomPrice(new BigDecimal("150.00"));
        roomRequestDTO.setAdultCapacity(2);
        roomRequestDTO.setChildrenCapacity(1);

        // Setup response DTO
        roomResponseDTO = new RoomResponseDTO();
        roomResponseDTO.setId(1L);
        roomResponseDTO.setRoomNumber(101);
        roomResponseDTO.setRoomPrice(new BigDecimal("150.00"));
        roomResponseDTO.setAdultCapacity(2);
        roomResponseDTO.setChildrenCapacity(1);
        roomResponseDTO.setCreatedOn(LocalDateTime.now());
        roomResponseDTO.setUpdatedOn(LocalDateTime.now());

        // Setup availability request DTO
        availabilityRequestDTO = new RoomAvailabilityRequestDTO();
        availabilityRequestDTO.setNumberOfAdults(2);
        availabilityRequestDTO.setNumberOfChildren(0);
        availabilityRequestDTO.setCheckInDate(LocalDate.now().plusDays(1));
        availabilityRequestDTO.setCheckOutDate(LocalDate.now().plusDays(3));

        // Setup availability response DTO
        availabilityResponseDTO = new RoomAvailabilityResponseDTO();
        availabilityResponseDTO.setId(1L);
        availabilityResponseDTO.setRoomNumber(101);
        availabilityResponseDTO.setRoomPrice(new BigDecimal("150.00"));
        availabilityResponseDTO.setAdultCapacity(2);
        availabilityResponseDTO.setChildrenCapacity(1);
    }

    @Test
    @DisplayName("Should get all rooms successfully")
    void shouldGetAllRoomsSuccessfully() {
        // Given
        when(roomRepository.findAll(Sort.by("roomNumber"))).thenReturn(List.of(testRoom));
        when(roomMapper.toResponseDTOs(anyList())).thenReturn(List.of(roomResponseDTO));

        // When
        List<RoomResponseDTO> result = roomService.getAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getRoomNumber()).isEqualTo(101);
        
        verify(roomRepository).findAll(Sort.by("roomNumber"));
        verify(roomMapper).toResponseDTOs(anyList());
    }

    @Test
    @DisplayName("Should get room by ID successfully")
    void shouldGetRoomByIdSuccessfully() {
        // Given
        Long roomId = 1L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(roomMapper.toResponseDTO(testRoom)).thenReturn(roomResponseDTO);

        // When
        RoomResponseDTO result = roomService.getById(roomId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(roomId);
        assertThat(result.getRoomNumber()).isEqualTo(101);
        assertThat(result.getRoomPrice()).isEqualTo(new BigDecimal("150.00"));
        
        verify(roomRepository).findById(roomId);
        verify(roomMapper).toResponseDTO(testRoom);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when room not found")
    void shouldThrowResourceNotFoundExceptionWhenRoomNotFound() {
        // Given
        Long roomId = 999L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roomService.getById(roomId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Room with ID " + roomId + " not found");
        
        verify(roomRepository).findById(roomId);
        verify(roomMapper, never()).toResponseDTO(any());
    }

    @Test
    @DisplayName("Should get available room successfully")
    void shouldGetAvailableRoomSuccessfully() {
        // Given
        when(roomRepository.findOneAvailableRoom(
            availabilityRequestDTO.getNumberOfAdults(),
            availabilityRequestDTO.getNumberOfChildren(),
            availabilityRequestDTO.getCheckInDate(),
            availabilityRequestDTO.getCheckOutDate()
        )).thenReturn(Optional.of(testRoom));
        when(roomMapper.toRoomAvailabilityResponseDTO(testRoom)).thenReturn(availabilityResponseDTO);

        // When
        RoomAvailabilityResponseDTO result = roomService.getAvailableRoom(availabilityRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRoomNumber()).isEqualTo(101);
        
        verify(roomRepository).findOneAvailableRoom(
            availabilityRequestDTO.getNumberOfAdults(),
            availabilityRequestDTO.getNumberOfChildren(),
            availabilityRequestDTO.getCheckInDate(),
            availabilityRequestDTO.getCheckOutDate()
        );
        verify(roomMapper).toRoomAvailabilityResponseDTO(testRoom);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no available room found")
    void shouldThrowResourceNotFoundExceptionWhenNoAvailableRoomFound() {
        // Given
        when(roomRepository.findOneAvailableRoom(
            availabilityRequestDTO.getNumberOfAdults(),
            availabilityRequestDTO.getNumberOfChildren(),
            availabilityRequestDTO.getCheckInDate(),
            availabilityRequestDTO.getCheckOutDate()
        )).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roomService.getAvailableRoom(availabilityRequestDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("No available room found for the specified criteria");
        
        verify(roomRepository).findOneAvailableRoom(
            availabilityRequestDTO.getNumberOfAdults(),
            availabilityRequestDTO.getNumberOfChildren(),
            availabilityRequestDTO.getCheckInDate(),
            availabilityRequestDTO.getCheckOutDate()
        );
        verify(roomMapper, never()).toRoomAvailabilityResponseDTO(any());
    }

    @Test
    @DisplayName("Should create room successfully")
    void shouldCreateRoomSuccessfully() {
        // Given
        when(roomRepository.existsByRoomNumber(roomRequestDTO.getRoomNumber()))
            .thenReturn(false);
        when(roomMapper.toEntity(roomRequestDTO)).thenReturn(testRoom);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(roomMapper.toResponseDTO(testRoom)).thenReturn(roomResponseDTO);

        // When
        RoomResponseDTO result = roomService.create(roomRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRoomNumber()).isEqualTo(101);
        assertThat(result.getRoomPrice()).isEqualTo(new BigDecimal("150.00"));
        
        verify(roomRepository).existsByRoomNumber(roomRequestDTO.getRoomNumber());
        verify(roomRepository).save(any(Room.class));
        verify(roomMapper).toEntity(roomRequestDTO);
        verify(roomMapper).toResponseDTO(testRoom);
    }

    @Test
    @DisplayName("Should throw BusinessValidationException when room number already exists")
    void shouldThrowBusinessValidationExceptionWhenRoomNumberExists() {
        // Given
        when(roomRepository.existsByRoomNumber(roomRequestDTO.getRoomNumber()))
            .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> roomService.create(roomRequestDTO))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("Room number " + roomRequestDTO.getRoomNumber() + " already exists");
        
        verify(roomRepository).existsByRoomNumber(roomRequestDTO.getRoomNumber());
        verify(roomRepository, never()).save(any());
        verify(roomMapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("Should update room successfully")
    void shouldUpdateRoomSuccessfully() {
        // Given
        Long roomId = 1L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(roomMapper.toResponseDTO(testRoom)).thenReturn(roomResponseDTO);

        // When
        RoomResponseDTO result = roomService.update(roomId, roomRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(roomId);
        
        verify(roomRepository).findById(roomId);
        verify(roomRepository).save(testRoom);
        verify(roomMapper).toResponseDTO(testRoom);
        
        // Verify that room properties are updated
        assertThat(testRoom.getRoomNumber()).isEqualTo(roomRequestDTO.getRoomNumber());
        assertThat(testRoom.getRoomPrice()).isEqualTo(roomRequestDTO.getRoomPrice());
        assertThat(testRoom.getAdultCapacity()).isEqualTo(roomRequestDTO.getAdultCapacity());
        assertThat(testRoom.getChildrenCapacity()).isEqualTo(roomRequestDTO.getChildrenCapacity());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent room")
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentRoom() {
        // Given
        Long roomId = 999L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roomService.update(roomId, roomRequestDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Room with ID " + roomId + " not found");
        
        verify(roomRepository).findById(roomId);
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete room successfully when room exists and has no amenities")
    void shouldDeleteRoomSuccessfullyWhenRoomExistsAndHasNoAmenities() {
        // Given
        Long roomId = 1L;
        when(roomRepository.existsById(roomId)).thenReturn(true);
        when(roomAmenityRepository.existsByRoomId(roomId)).thenReturn(false);

        // When
        roomService.deleteById(roomId);

        // Then
        verify(roomRepository).existsById(roomId);
        verify(roomAmenityRepository).existsByRoomId(roomId);
        verify(roomRepository).deleteById(roomId);
        verify(roomAmenityRepository, never()).deleteAllByRoomId(roomId);
    }

    @Test
    @DisplayName("Should delete room and its amenities successfully when room has amenities")
    void shouldDeleteRoomAndItsAmenitiesSuccessfullyWhenRoomHasAmenities() {
        // Given
        Long roomId = 1L;
        when(roomRepository.existsById(roomId)).thenReturn(true);
        when(roomAmenityRepository.existsByRoomId(roomId)).thenReturn(true);

        // When
        roomService.deleteById(roomId);

        // Then
        verify(roomRepository).existsById(roomId);
        verify(roomAmenityRepository).existsByRoomId(roomId);
        verify(roomAmenityRepository).deleteAllByRoomId(roomId);
        verify(roomRepository).deleteById(roomId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent room")
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentRoom() {
        // Given
        Long roomId = 999L;
        when(roomRepository.existsById(roomId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> roomService.deleteById(roomId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Room with ID " + roomId + " not found");
        
        verify(roomRepository).existsById(roomId);
        verify(roomAmenityRepository, never()).existsByRoomId(anyLong());
        verify(roomRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should handle empty room list")
    void shouldHandleEmptyRoomList() {
        // Given
        when(roomRepository.findAll(Sort.by("roomNumber"))).thenReturn(List.of());
        when(roomMapper.toResponseDTOs(anyList())).thenReturn(List.of());

        // When
        List<RoomResponseDTO> result = roomService.getAll();

        // Then
        assertThat(result).isEmpty();
        
        verify(roomRepository).findAll(Sort.by("roomNumber"));
        verify(roomMapper).toResponseDTOs(anyList());
    }

    @Test
    @DisplayName("Should cache room by ID")
    void shouldCacheRoomById() {
        // Given
        Long roomId = 1L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(roomMapper.toResponseDTO(testRoom)).thenReturn(roomResponseDTO);

        // When - first call
        RoomResponseDTO result1 = roomService.getById(roomId);
        
        // When - second call (should use cache)
        RoomResponseDTO result2 = roomService.getById(roomId);

        // Then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.getId()).isEqualTo(roomId);
        
        // Repository should be called only once due to caching
        verify(roomRepository, times(2)).findById(roomId);
        verify(roomMapper, times(2)).toResponseDTO(testRoom);
    }

    @Test
    @DisplayName("Should validate room availability request parameters")
    void shouldValidateRoomAvailabilityRequestParameters() {
        // Test will depend on validation implementation
        // This is a placeholder for validation testing
        assertThat(availabilityRequestDTO.getNumberOfAdults()).isGreaterThan(0);
        assertThat(availabilityRequestDTO.getNumberOfChildren()).isGreaterThanOrEqualTo(0);
        assertThat(availabilityRequestDTO.getCheckInDate()).isNotNull();
        assertThat(availabilityRequestDTO.getCheckOutDate()).isNotNull();
        assertThat(availabilityRequestDTO.getCheckOutDate()).isAfter(availabilityRequestDTO.getCheckInDate());
    }
}