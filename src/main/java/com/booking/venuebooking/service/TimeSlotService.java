package com.booking.venuebooking.service;


import com.booking.venuebooking.dto.TimeSlotDTO;
import com.booking.venuebooking.entity.TimeSlot;
import com.booking.venuebooking.entity.Venue;
import com.booking.venuebooking.exception.ResourceNotFoundException;
import com.booking.venuebooking.exception.SlotOverlapException;
import com.booking.venuebooking.repository.TimeSlotRepository;
import com.booking.venuebooking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final VenueRepository venueRepository;

    @Transactional
    public TimeSlotDTO createSlot(Long venueId, TimeSlotDTO dto) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));


        if (dto.getEndTime().isBefore(dto.getStartTime()) || dto.getEndTime().isEqual(dto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }


        boolean hasOverlap = timeSlotRepository.existsOverlappingSlot(
                venueId, dto.getStartTime(), dto.getEndTime()
        );

        if (hasOverlap) {
            throw new SlotOverlapException("Time slot overlaps with existing slot for this venue");
        }

        TimeSlot slot = new TimeSlot();
        slot.setVenue(venue);
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setIsAvailable(true);

        slot = timeSlotRepository.save(slot);
        return mapToDTO(slot);
    }

    @Transactional(readOnly = true)
    public List<TimeSlotDTO> getSlotsByVenue(Long venueId) {
        if (!venueRepository.existsById(venueId)) {
            throw new ResourceNotFoundException("Venue not found with id: " + venueId);
        }

        return timeSlotRepository.findByVenueId(venueId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimeSlotDTO> getAvailableSlots(String sportId, LocalDateTime startTime, LocalDateTime endTime) {
        return timeSlotRepository.findAvailableSlotsByTimeRange(sportId, startTime, endTime).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TimeSlotDTO mapToDTO(TimeSlot slot) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(slot.getId());
        dto.setVenueId(slot.getVenue().getId());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setIsAvailable(slot.getIsAvailable());
        return dto;
    }
}