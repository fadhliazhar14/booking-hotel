package com.fadhliazhar.booking_hotel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class RoomService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_service_type")
    private RoomServiceType roomServiceType;

    @Column(name = "booking_id")
    private Long bookingId;

    private LocalDate date;

    private BigDecimal amount;
}
