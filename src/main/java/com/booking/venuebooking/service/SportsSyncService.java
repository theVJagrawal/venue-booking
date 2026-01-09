package com.booking.venuebooking.service;

import com.booking.venuebooking.dto.SportDTO;
import com.booking.venuebooking.dto.SportsApiResponse;
import com.booking.venuebooking.entity.Sport;
import com.booking.venuebooking.repository.SportRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SportsSyncService {

    private final SportRepository sportRepository;
    private final RestTemplate restTemplate;

    @Value("${sports.api.url}")
    private String sportsApiUrl;

    public SportsSyncService(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void loadSportsOnStartup() {

        SportsApiResponse response =
                restTemplate.getForObject(sportsApiUrl, SportsApiResponse.class);

        if (response == null || response.getData() == null) {
            return; // assignment me silent fail ok
        }

        for (SportDTO dto : response.getData()) {

            // avoid duplicate insert
            boolean exists = sportRepository
                    .existsBySportId(dto.getSport_id());

            if (!exists) {
                Sport sport = new Sport();
                sport.setSportId(dto.getSport_id());
                sport.setSportCode(dto.getSport_code());
                sport.setSportName(dto.getSport_name());

                sportRepository.save(sport);
            }
        }
    }
}