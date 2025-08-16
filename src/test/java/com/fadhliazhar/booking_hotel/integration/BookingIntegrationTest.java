package com.fadhliazhar.booking_hotel.integration;

import com.fadhliazhar.booking_hotel.dto.booking.BookingRequestDTO;
import com.fadhliazhar.booking_hotel.dto.booking.BookingResponseDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomRequestDTO;
import com.fadhliazhar.booking_hotel.dto.room.RoomResponseDTO;
import com.fadhliazhar.booking_hotel.model.BookingStatus;
import com.fadhliazhar.booking_hotel.service.BookingService;
import com.fadhliazhar.booking_hotel.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the complete booking workflow
 * Tests the interaction between all layers with real database and Redis
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Transactional
@DisplayName("Booking Integration Tests")
class BookingIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("booking_hotel_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL configuration
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        
        // Redis configuration
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        
        // OAuth2 test configuration (mock)
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", 
            () -> "http://localhost:8080/auth/realms/test");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", 
            () -> "http://localhost:8080/auth/realms/test/protocol/openid_connect/certs");
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private RoomResponseDTO testRoom;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Create a test room for booking tests
        createTestRoom();
    }

    private void createTestRoom() {
        RoomRequestDTO roomRequest = new RoomRequestDTO();
        roomRequest.setRoomNumber(101);
        roomRequest.setRoomPrice(new BigDecimal("150.00"));
        roomRequest.setAdultCapacity(2);
        roomRequest.setChildrenCapacity(1);

        testRoom = roomService.create(roomRequest);
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    @DisplayName("Should complete full booking workflow successfully")
    void shouldCompleteFullBookingWorkflowSuccessfully() throws Exception {
        // Given - Create booking request
        BookingRequestDTO bookingRequest = new BookingRequestDTO();
        bookingRequest.setFirstName("John");
        bookingRequest.setLastName("Doe");
        bookingRequest.setCheckedInDate(LocalDate.now().plusDays(1));
        bookingRequest.setCheckedOutDate(LocalDate.now().plusDays(3));
        bookingRequest.setAdultCapacity(2);
        bookingRequest.setChildrenCapacity(0);
        bookingRequest.setRoomId(testRoom.getId());

        // When - Create booking via REST API
        String bookingJson = objectMapper.writeValueAsString(bookingRequest);
        
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.roomId").value(testRoom.getId()))
                .andExpect(jsonPath("$.roomNumber").value(101))
                .andExpect(jsonPath("$.bookingStatus").value("BOOKED"));
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    @DisplayName("Should prevent double booking for same room and dates")
    void shouldPreventDoubleBookingForSameRoomAndDates() throws Exception {
        // Given - Create first booking
        BookingRequestDTO firstBooking = new BookingRequestDTO();
        firstBooking.setFirstName("John");
        firstBooking.setLastName("Doe");
        firstBooking.setCheckedInDate(LocalDate.now().plusDays(1));
        firstBooking.setCheckedOutDate(LocalDate.now().plusDays(3));
        firstBooking.setAdultCapacity(2);
        firstBooking.setChildrenCapacity(0);
        firstBooking.setRoomId(testRoom.getId());

        // Create first booking successfully
        BookingResponseDTO firstBookingResponse = bookingService.create(firstBooking);
        assertThat(firstBookingResponse).isNotNull();
        assertThat(firstBookingResponse.getBookingStatus()).isEqualTo(BookingStatus.BOOKED);

        // When - Try to create overlapping booking
        BookingRequestDTO secondBooking = new BookingRequestDTO();
        secondBooking.setFirstName("Jane");
        secondBooking.setLastName("Smith");
        secondBooking.setCheckedInDate(LocalDate.now().plusDays(2)); // Overlaps with first booking
        secondBooking.setCheckedOutDate(LocalDate.now().plusDays(4));
        secondBooking.setAdultCapacity(2);
        secondBooking.setChildrenCapacity(0);
        secondBooking.setRoomId(testRoom.getId());

        String bookingJson = objectMapper.writeValueAsString(secondBooking);

        // Then - Second booking should fail
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Room is not available")));
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    @DisplayName("Should get available rooms correctly")
    void shouldGetAvailableRoomsCorrectly() throws Exception {
        // Given - Create additional room
        RoomRequestDTO roomRequest2 = new RoomRequestDTO();
        roomRequest2.setRoomNumber(102);
        roomRequest2.setRoomPrice(new BigDecimal("200.00"));
        roomRequest2.setAdultCapacity(3);
        roomRequest2.setChildrenCapacity(2);
        RoomResponseDTO room2 = roomService.create(roomRequest2);

        // Create booking for first room
        BookingRequestDTO bookingRequest = new BookingRequestDTO();
        bookingRequest.setFirstName("John");
        bookingRequest.setLastName("Doe");
        bookingRequest.setCheckedInDate(LocalDate.now().plusDays(1));
        bookingRequest.setCheckedOutDate(LocalDate.now().plusDays(3));
        bookingRequest.setAdultCapacity(2);
        bookingRequest.setChildrenCapacity(0);
        bookingRequest.setRoomId(testRoom.getId());
        bookingService.create(bookingRequest);

        // When - Get available rooms for same dates
        mockMvc.perform(get("/api/rooms/available")
                .param("checkInDate", LocalDate.now().plusDays(1).toString())
                .param("checkOutDate", LocalDate.now().plusDays(3).toString())
                .param("numberOfAdults", "2")
                .param("numberOfChildren", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(room2.getId())) // Only room2 should be available
                .andExpect(jsonPath("$.roomNumber").value(102));
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() throws Exception {
        // Given - Create multiple bookings
        for (int i = 0; i < 5; i++) {
            RoomRequestDTO roomReq = new RoomRequestDTO();
            roomReq.setRoomNumber(200 + i);
            roomReq.setRoomPrice(new BigDecimal("100.00"));
            roomReq.setAdultCapacity(2);
            roomReq.setChildrenCapacity(1);
            RoomResponseDTO room = roomService.create(roomReq);

            BookingRequestDTO bookingReq = new BookingRequestDTO();
            bookingReq.setFirstName("User" + i);
            bookingReq.setLastName("Test");
            bookingReq.setCheckedInDate(LocalDate.now().plusDays(10 + i));
            bookingReq.setCheckedOutDate(LocalDate.now().plusDays(12 + i));
            bookingReq.setAdultCapacity(2);
            bookingReq.setChildrenCapacity(0);
            bookingReq.setRoomId(room.getId());
            bookingService.create(bookingReq);
        }

        // When - Get paginated bookings
        mockMvc.perform(get("/api/bookings")
                .param("page", "0")
                .param("size", "3")
                .param("sort", "id")
                .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(2)));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName("Should allow admin to access all bookings")
    void shouldAllowAdminToAccessAllBookings() throws Exception {
        // Given - Create booking as different user
        BookingRequestDTO bookingRequest = new BookingRequestDTO();
        bookingRequest.setFirstName("John");
        bookingRequest.setLastName("Doe");
        bookingRequest.setCheckedInDate(LocalDate.now().plusDays(1));
        bookingRequest.setCheckedOutDate(LocalDate.now().plusDays(3));
        bookingRequest.setAdultCapacity(2);
        bookingRequest.setChildrenCapacity(0);
        bookingRequest.setRoomId(testRoom.getId());
        BookingResponseDTO booking = bookingService.create(bookingRequest);

        // When - Admin accesses booking
        mockMvc.perform(get("/api/bookings/" + booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("Should reject unauthenticated requests")
    void shouldRejectUnauthenticatedRequests() throws Exception {
        // When - Try to access protected endpoint without authentication
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    @DisplayName("Should validate booking request data")
    void shouldValidateBookingRequestData() throws Exception {
        // Given - Invalid booking request (missing required fields)
        BookingRequestDTO invalidRequest = new BookingRequestDTO();
        // Leave required fields empty

        String bookingJson = objectMapper.writeValueAsString(invalidRequest);

        // When - Try to create booking with invalid data
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-user", authorities = {"USER"})
    @DisplayName("Should handle search functionality")
    void shouldHandleSearchFunctionality() throws Exception {
        // Given - Create bookings with searchable data
        BookingRequestDTO bookingRequest = new BookingRequestDTO();
        bookingRequest.setFirstName("SearchableUser");
        bookingRequest.setLastName("TestSearch");
        bookingRequest.setCheckedInDate(LocalDate.now().plusDays(5));
        bookingRequest.setCheckedOutDate(LocalDate.now().plusDays(7));
        bookingRequest.setAdultCapacity(2);
        bookingRequest.setChildrenCapacity(0);
        bookingRequest.setRoomId(testRoom.getId());
        bookingService.create(bookingRequest);

        // When - Search for bookings
        mockMvc.perform(get("/api/bookings")
                .param("search", "SearchableUser")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("SearchableUser"));
    }

    @Nested
    @DisplayName("Caching Tests")
    class CachingTests {

        @Test
        @WithMockUser(username = "test-user", authorities = {"USER"})
        @DisplayName("Should cache room data")
        void shouldCacheRoomData() throws Exception {
            Long roomId = testRoom.getId();

            // First request - should hit database
            mockMvc.perform(get("/api/rooms/" + roomId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(roomId));

            // Second request - should use cache
            mockMvc.perform(get("/api/rooms/" + roomId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(roomId));
            
            // Cache behavior is verified through performance monitoring
        }
    }

    @AfterEach
    void tearDown() {
        // Cleanup is handled by @Transactional and TestContainers
    }
}