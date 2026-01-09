package com.booking.venuebooking.service;


import com.booking.venuebooking.dto.VenueDTO;
import com.booking.venuebooking.entity.Sport;
import com.booking.venuebooking.entity.Venue;
import com.booking.venuebooking.exception.ResourceNotFoundException;
import com.booking.venuebooking.repository.SportRepository;
import com.booking.venuebooking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final RestTemplate restTemplate;
    private final SportRepository sportRepository;



    @Transactional
    public VenueDTO createVenue(VenueDTO dto) {

        Sport sport = validateAndGetSport(dto.getSportName());

        Venue venue = new Venue();
        venue.setName(dto.getName());
        venue.setLocation(dto.getLocation());
        venue.setSportId(sport.getSportId());
        venue.setSportName(sport.getSportName());

        venue = venueRepository.save(venue);
        return mapToDTO(venue);
    }

    @Transactional(readOnly = true)
    public List<VenueDTO> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VenueDTO getVenueById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
        return mapToDTO(venue);
    }

    @Transactional
    public void deleteVenue(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venue not found with id: " + id);
        }
        venueRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public List<VenueDTO> getAvailableVenues() {
        return venueRepository.findVenuesWithAvailableSlots().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    private List<String> getAllSportNames() {
        return sportRepository.findAll()
                .stream()
                .map(Sport::getSportName)
                .toList();
    }


    private Sport validateAndGetSport(String sportName) {

        return sportRepository
                .findBySportNameIgnoreCase(sportName)
                .orElseThrow(() -> {
                    List<String> availableSports = getAllSportNames();
                    throw new ResourceNotFoundException(
                            "Invalid sport. Please select from available sports: " + availableSports
                    );
                });
    }

    private VenueDTO mapToDTO(Venue venue) {
        VenueDTO dto = new VenueDTO();
        dto.setId(venue.getId());
        dto.setName(venue.getName());
        dto.setLocation(venue.getLocation());
        dto.setSportId(venue.getSportId());
        dto.setSportName(venue.getSportName());
        // Count available slots for this venue
        long availableCount = venue.getTimeSlots().stream()
                .filter(slot -> slot.getIsAvailable() != null && slot.getIsAvailable())
                .count();
        dto.setAvailableSlotsCount((int) availableCount);
        return dto;
    }
}