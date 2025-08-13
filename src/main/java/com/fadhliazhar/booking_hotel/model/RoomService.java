package com.fadhliazhar.booking_hotel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "room_services")
public class RoomService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Service type is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @NotNull(message = "Room is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @NotNull(message = "Service date is required")
    @Column(name = "service_date", nullable = false)
    private LocalDate date;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Positive(message = "Quantity must be positive")
    @Column(name = "quantity")
    private Integer quantity = 1;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ServiceStatus status = ServiceStatus.REQUESTED;

    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @UpdateTimestamp
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    public RoomService(ServiceType serviceType, Room room, LocalDate date, BigDecimal amount) {
        this.serviceType = serviceType;
        this.room = room;
        this.date = date;
        this.amount = amount;
        this.quantity = 1;
        this.status = ServiceStatus.REQUESTED;
    }

    // For backward compatibility
    public RoomService(ServiceType serviceType, Long bookingId, LocalDate date, BigDecimal amount) {
        this.serviceType = serviceType;
        if (bookingId != null) {
            Booking tempBooking = new Booking();
            tempBooking.setId(bookingId);
            this.booking = tempBooking;
        }
        this.date = date;
        this.amount = amount;
        this.quantity = 1;
        this.status = ServiceStatus.REQUESTED;
    }

    public Long getBookingId() {
        return this.booking != null ? this.booking.getId() : null;
    }

    public void setBookingId(Long bookingId) {
        if (bookingId != null) {
            Booking tempBooking = new Booking();
            tempBooking.setId(bookingId);
            this.booking = tempBooking;
        }
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

    // Calculate total amount based on quantity
    public BigDecimal getTotalAmount() {
        if (amount != null && quantity != null) {
            return amount.multiply(BigDecimal.valueOf(quantity));
        }
        return amount;
    }

    @Override
    public String toString() {
        return "RoomService{" +
                "id=" + id +
                ", serviceType=" + (serviceType != null ? serviceType.getName() : null) +
                ", roomId=" + getRoomId() +
                ", bookingId=" + getBookingId() +
                ", date=" + date +
                ", amount=" + amount +
                ", quantity=" + quantity +
                ", status=" + status +
                '}';
    }

    // Enum for service status
    public enum ServiceStatus {
        REQUESTED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
