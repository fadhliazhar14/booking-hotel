package com.fadhliazhar.booking_hotel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class RoomService {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "room_service_type")
    private RoomServiceType roomServiceType;

    @Column(name = "booking_id")
    private Long bookingId;

    private LocalDate date;

    private BigDecimal amount;
}
