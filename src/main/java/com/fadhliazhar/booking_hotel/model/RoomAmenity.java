package com.fadhliazhar.booking_hotel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "room_amenities", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_id", "amenity_type_id"})
})
public class RoomAmenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Amenity type is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amenity_type_id", nullable = false)
    private AmenityType amenityType;

    @NotNull(message = "Room is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @UpdateTimestamp
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    public RoomAmenity(AmenityType amenityType, Room room) {
        this.amenityType = amenityType;
        this.room = room;
        this.isAvailable = true;
    }

    // For backward compatibility
    public RoomAmenity(AmenityType amenityType, Long roomId) {
        this.amenityType = amenityType;
        if (roomId != null) {
            Room tempRoom = new Room();
            tempRoom.setId(roomId);
            this.room = tempRoom;
        }
        this.isAvailable = true;
    }

    public Long getRoomId() {
        return this.room != null ? this.room.getId() : null;
    }

    public void setRoomId(Long roomId) {
        if (roomId != null) {
            Room tempRoom = new Room();
            tempRoom.setId(roomId);
            this.room = tempRoom;
        }
    }

    @Override
    public String toString() {
        return "RoomAmenity{" +
                "id=" + id +
                ", amenityType=" + (amenityType != null ? amenityType.getName() : null) +
                ", roomId=" + getRoomId() +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
