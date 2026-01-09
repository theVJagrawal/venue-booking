package com.booking.venuebooking.repository;

import com.booking.venuebooking.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SportRepository extends JpaRepository<Sport, String> {

    boolean existsBySportId(String sportId);

    Optional<Sport> findBySportNameIgnoreCase(String sportName);
}
