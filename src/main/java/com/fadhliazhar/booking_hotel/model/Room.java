package com.fadhliazhar.booking_hotel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rooms", uniqueConstraints = {
    @UniqueConstraint(columnNames = "room_number")
})
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Room number is required")
    @Positive(message = "Room number must be positive")
    @Column(name = "room_number", nullable = false, unique = true)
    private Integer roomNumber;

    @NotNull(message = "Room price is required")
    @Positive(message = "Room price must be positive")
    @Column(name = "room_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal roomPrice;

    @NotNull(message = "Adult capacity is required")
    @Positive(message = "Adult capacity must be positive")
    @Column(name = "adult_capacity", nullable = false)
    private Integer adultCapacity;

    @Column(name = "children_capacity")
    private Integer childrenCapacity = 0;

    @Column(name = "room_description", columnDefinition = "TEXT")
    private String roomDescription;

    @Column(name = "room_type")
    private String roomType;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    // Bidirectional relationship with Booking
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    // Bidirectional relationship with RoomAmenity
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoomAmenity> roomAmenities = new ArrayList<>();

    // Bidirectional relationship with RoomService
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoomService> roomServices = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods for managing relationships
    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setRoom(this);
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setRoom(null);
    }

    public void addRoomAmenity(RoomAmenity roomAmenity) {
        roomAmenities.add(roomAmenity);
        roomAmenity.setRoom(this);
    }

    public void removeRoomAmenity(RoomAmenity roomAmenity) {
        roomAmenities.remove(roomAmenity);
        roomAmenity.setRoom(null);
    }

    public void addRoomService(RoomService roomService) {
        roomServices.add(roomService);
        roomService.setRoom(this);
    }

    public void removeRoomService(RoomService roomService) {
        roomServices.remove(roomService);
        roomService.setRoom(null);
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", roomNumber=" + roomNumber +
                ", roomPrice=" + roomPrice +
                ", adultCapacity=" + adultCapacity +
                ", childrenCapacity=" + childrenCapacity +
                ", roomType='" + roomType + '\'' +
                ", floorNumber=" + floorNumber +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
