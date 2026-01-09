package com.booking.venuebooking.repository;

import com.booking.venuebooking.entity.TimeSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    @Query("SELECT COUNT(ts) > 0 FROM TimeSlot ts WHERE ts.venue.id = :venueId " +
            "AND ((ts.startTime < :endTime AND ts.endTime > :startTime))")
    boolean existsOverlappingSlot(@Param("venueId") Long venueId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.id = :id")
    Optional<TimeSlot> findByIdWithLock(@Param("id") Long id);

    List<TimeSlot> findByVenueId(Long venueId);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.venue.sportId = :sportId " +
            "AND ts.isAvailable = true AND ts.startTime >= :startTime " +
            "AND ts.endTime <= :endTime")
    List<TimeSlot> findAvailableSlotsByTimeRange(@Param("sportId") String sportId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);
}
