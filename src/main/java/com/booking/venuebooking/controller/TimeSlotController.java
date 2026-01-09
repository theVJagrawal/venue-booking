package com.booking.venuebooking.controller;


import com.booking.venuebooking.dto.TimeSlotDTO;
import com.booking.venuebooking.service.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/venues/{venueId}/slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @PostMapping
    public ResponseEntity<TimeSlotDTO> createSlot(
            @PathVariable Long venueId,
            @Valid @RequestBody TimeSlotDTO dto) {

        TimeSlotDTO created = timeSlotService.createSlot(venueId, dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TimeSlotDTO>> getSlotsByVenue(@PathVariable Long venueId) {
        List<TimeSlotDTO> slots = timeSlotService.getSlotsByVenue(venueId);
        return ResponseEntity.ok(slots);
    }
}