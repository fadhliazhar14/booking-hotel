package com.fadhliazhar.booking_hotel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @NotNull(message = "Check-in date is required")
    @Column(name = "checked_in_date", nullable = false)
    private LocalDate checkedInDate;

    @NotNull(message = "Check-out date is required")
    @Column(name = "checked_out_date", nullable = false)
    private LocalDate checkedOutDate;

    @Positive(message = "Adult capacity must be positive")
    @Column(name = "adult_capacity", nullable = false)
    private Integer adultCapacity;

    @Column(name = "children_capacity")
    private Integer childrenCapacity = 0;

    @Transient
    private int night;

    // Proper JPA relationship with Room entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull(message = "Booking status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus = BookingStatus.BOOKED;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "user_id")
    private String userId; // OAuth2 user ID from JWT token

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public int getNight() {
        if (this.checkedInDate != null && this.checkedOutDate != null) {
            long diffInDays = ChronoUnit.DAYS.between(this.checkedInDate, this.checkedOutDate);
            return (int) Math.max(diffInDays, 1); // Minimum 1 night
        }
        return 0;
    }

    public Long getRoomId() {
        return this.room != null ? this.room.getId() : null;
    }
    
    public void setRoomId(Long roomId) {
        // This method is for backward compatibility with DTOs
        // The actual room relationship should be set via setRoom method
        if (roomId != null && this.room == null) {
            Room tempRoom = new Room();
            tempRoom.setId(roomId);
            this.room = tempRoom;
        }
    }

    // Calculate total amount based on room price and nights
    public void calculateTotalAmount() {
        if (this.room != null && this.room.getRoomPrice() != null) {
            BigDecimal pricePerNight = this.room.getRoomPrice();
            int nights = getNight();
            this.totalAmount = pricePerNight.multiply(BigDecimal.valueOf(nights));
        }
    }

    @PrePersist
    @PreUpdate
    private void calculateAmountBeforeSave() {
        calculateTotalAmount();
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", checkedInDate=" + checkedInDate +
                ", checkedOutDate=" + checkedOutDate +
                ", adultCapacity=" + adultCapacity +
                ", childrenCapacity=" + childrenCapacity +
                ", night=" + getNight() +
                ", roomId=" + getRoomId() +
                ", bookingStatus=" + bookingStatus +
                ", totalAmount=" + totalAmount +
                ", userId='" + userId + '\'' +
                '}';
    }
}
