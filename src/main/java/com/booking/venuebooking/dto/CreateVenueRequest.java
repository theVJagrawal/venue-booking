package com.booking.venuebooking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Request DTO for creating venue
@Data
@NoArgsConstructor
@AllArgsConstructor
class CreateVenueRequest {
    @NotBlank(message = "Venue name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Sport ID is required")
    private String sportId;
}
