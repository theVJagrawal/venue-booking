package com.booking.venuebooking.service;


import com.booking.venuebooking.dto.BookingDTO;
import com.booking.venuebooking.dto.TimeSlotDTO;
import com.booking.venuebooking.entity.Booking;
import com.booking.venuebooking.entity.TimeSlot;
import com.booking.venuebooking.exception.BookingException;
import com.booking.venuebooking.exception.ResourceNotFoundException;
import com.booking.venuebooking.repository.BookingRepository;
import com.booking.venuebooking.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingDTO createBooking(BookingDTO dto) {
        // Use pessimistic lock to prevent double booking
        TimeSlot slot = timeSlotRepository.findByIdWithLock(dto.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found with id: " + dto.getSlotId()));

        // Check if slot is available
        if (!slot.getIsAvailable()) {
            throw new BookingException("Time slot is not available for booking");
        }

        // Check if slot already has an ACTIVE (CONFIRMED) booking
        Optional<Booking> activeBooking = bookingRepository.findActiveBookingBySlotId(slot.getId());
        if (activeBooking.isPresent()) {
            throw new BookingException("Time slot is already booked");
        }

        // Create booking
        log.debug("booking trying to create ");
        System.out.println("booking trying to create ");
        Booking booking = new Booking();
        booking.setTimeSlot(slot);
        booking.setCustomerName(dto.getCustomerName());
        booking.setCustomerEmail(dto.getCustomerEmail());
        booking.setCustomerPhone(dto.getCustomerPhone());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);


        // Mark slot as unavailable
        slot.setIsAvailable(false);
        System.out.println("booking trying to create save timeslot ");
        timeSlotRepository.save(slot);
        System.out.println("booking trying to create save bookingRepository ");
        booking = bookingRepository.save(booking);
        System.out.println("booking trying to create save final ");
        return mapToDTO(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return mapToDTO(booking);
    }

    @Transactional
    public BookingDTO cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        // Free up the slot
        TimeSlot slot = booking.getTimeSlot();
        slot.setIsAvailable(true);
        timeSlotRepository.save(slot);

        booking = bookingRepository.save(booking);
        return mapToDTO(booking);
    }

    private BookingDTO mapToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setSlotId(booking.getTimeSlot().getId());
        dto.setCustomerName(booking.getCustomerName());
        dto.setCustomerEmail(booking.getCustomerEmail());
        dto.setCustomerPhone(booking.getCustomerPhone());
        dto.setStatus(booking.getStatus().name());
        dto.setBookingDate(booking.getBookingDate());

        // Include slot details
        TimeSlot slot = booking.getTimeSlot();
        TimeSlotDTO slotDTO = new TimeSlotDTO();
        slotDTO.setId(slot.getId());
        slotDTO.setVenueId(slot.getVenue().getId());
        slotDTO.setStartTime(slot.getStartTime());
        slotDTO.setEndTime(slot.getEndTime());
        slotDTO.setIsAvailable(slot.getIsAvailable());
        dto.setSlot(slotDTO);

        return dto;
    }
}