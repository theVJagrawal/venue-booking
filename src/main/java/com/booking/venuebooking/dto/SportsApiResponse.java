package com.booking.venuebooking.dto;

import lombok.Data;

import java.util.List;

@Data
public class SportsApiResponse {
    private String status;
    private String msg;
    private List<SportDTO> data;
}
