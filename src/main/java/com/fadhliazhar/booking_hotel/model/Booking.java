package com.fadhliazhar.booking_hotel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "checked_in_date")
    private LocalDate checkedInDate;

    @Column(name = "checked_out_date")
    private LocalDate checkedOutDate;

    @Column(name = "adult_capacity")
    private Integer adultCapacity;

    @Column(name = "children_capacity")
    private Integer childrenCapacity;

    @Transient
    private int night;

    @Column(name = "room_id")
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus bookingStatus;

    public int getNight() {
        if (this.checkedInDate != null && this.checkedOutDate != null) {
            long diffInDays = ChronoUnit.DAYS.between(this.checkedInDate, this.checkedOutDate);
            return (int) diffInDays;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", checkedInDate=" + checkedInDate +
                ", checkedOutDate=" + checkedOutDate +
                ", adultCapacity=" + adultCapacity +
                ", childrenCapacity=" + childrenCapacity +
                ", night=" + night +
                ", roomId=" + roomId +
                ", bookingStatus=" + bookingStatus +
                '}';
    }
}
