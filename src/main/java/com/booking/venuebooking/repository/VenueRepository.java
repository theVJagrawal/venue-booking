package com.booking.venuebooking.repository;

import com.booking.venuebooking.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findBySportId(String sportId);

    @Query("SELECT DISTINCT v FROM Venue v LEFT JOIN FETCH v.timeSlots ts " +
            "WHERE ts.isAvailable = true")
    List<Venue> findVenuesWithAvailableSlots();
}
