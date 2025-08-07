package com.fadhliazhar.booking_hotel.service;

import com.fadhliazhar.booking_hotel.dto.booking.BookingRequestDTO;
import com.fadhliazhar.booking_hotel.dto.booking.BookingResponseDTO;
import com.fadhliazhar.booking_hotel.dto.booking.BookingStatusUpdateDTO;
import com.fadhliazhar.booking_hotel.exception.BusinessValidationException;
import com.fadhliazhar.booking_hotel.exception.ResourceNotFoundException;
import com.fadhliazhar.booking_hotel.mapper.BookingMapper;
import com.fadhliazhar.booking_hotel.model.Booking;
import com.fadhliazhar.booking_hotel.model.BookingStatus;
import com.fadhliazhar.booking_hotel.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class BookingService {
    private final BookingRepository bookingRepository;

    private final BookingMapper bookingMapper;

    public List<BookingResponseDTO> getAll() {
        return bookingMapper.toResponseDTOs(bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
    }

    public BookingResponseDTO getById(Long bookingId) {
        return bookingMapper.toResponseDTO(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + bookingId + " not found.")
                ));
    }

    public BookingResponseDTO create(BookingRequestDTO requestedBooking) {
        Booking bookingEntity = bookingMapper.toEntity(requestedBooking);
        Booking savedBooking  = bookingRepository.save(bookingEntity);

        return bookingMapper.toResponseDTO(savedBooking);
    }

    public BookingResponseDTO update(Long bookingId, BookingRequestDTO requestedBooking) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + bookingId + " not found."));

        booking.setFirstName(requestedBooking.getFirstName());
        booking.setLastName(requestedBooking.getLastName());
        booking.setCheckedInDate(requestedBooking.getCheckedInDate());
        booking.setCheckedOutDate(requestedBooking.getCheckedOutDate());
        booking.setAdultCapacity(requestedBooking.getAdultCapacity());
        booking.setChildrenCapacity(requestedBooking.getChildrenCapacity());

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponseDTO(savedBooking);
    }

    public BookingResponseDTO updateStatus(Long bookingId, BookingStatusUpdateDTO requestedStatus) {
        return bookingMapper.toResponseDTO(
                bookingRepository.findById(bookingId)
                    .map(booking -> {
                        BookingStatus currentStatus = booking.getBookingStatus();
                        BookingStatus newStatus = requestedStatus.getBookingStatus();

                        checkStatus(currentStatus, newStatus);
                        booking.setBookingStatus(newStatus);

                        return bookingRepository.save(booking);
                    })
                    .orElseThrow(() -> new RuntimeException("Booking with ID " + bookingId + " not found."))
        );
    }

    public void deleteById(Long bookingId) {
        boolean bookingExist = bookingRepository.existsById(bookingId);
        if (!bookingExist) {
            throw new ResourceNotFoundException("Booking with ID " + bookingId + " not found.");
        }

        bookingRepository.deleteById(bookingId);
    }

    private void checkStatus(BookingStatus currentStatus, BookingStatus newStatus) {
        // Validasi transisi status
        if (currentStatus == BookingStatus.CANCELED) {
            throw new BusinessValidationException("Cannot change status of a canceled booking.");
        }

        // Booking hanya bisa CHECKED_OUT jika sudah CHECKED_IN
        if (newStatus == BookingStatus.CHECKED_OUT && currentStatus != BookingStatus.CHECKED_IN) {
            throw new BusinessValidationException("Cannot check out a booking that is not checked in.");
        }

        // Booking tidak bisa cancel jika sudah CHECKED_IN atau CHECKED_OUT
        if (newStatus == BookingStatus.CANCELED && (currentStatus == BookingStatus.CHECKED_IN || currentStatus == BookingStatus.CHECKED_OUT)) {
            throw new BusinessValidationException("Cannot cancel a booking that has been checked in or checked out.");
        }
    }
}
