package com.booking.venuebooking.repository;

import com.booking.venuebooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerEmail(String email);

    Optional<Booking> findByTimeSlotId(Long slotId);

    @Query("SELECT b FROM Booking b WHERE b.timeSlot.id = :slotId AND b.status = 'CONFIRMED'")
    Optional<Booking> findActiveBookingBySlotId(@Param("slotId") Long slotId);

    List<Booking> findByStatus(Booking.BookingStatus status);
}
