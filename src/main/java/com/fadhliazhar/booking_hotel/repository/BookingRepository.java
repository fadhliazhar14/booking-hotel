package com.fadhliazhar.booking_hotel.repository;

import com.fadhliazhar.booking_hotel.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}
