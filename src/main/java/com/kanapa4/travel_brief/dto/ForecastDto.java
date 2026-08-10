package com.kanapa4.travel_brief.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ForecastDto {
    private String date;
    private Double tempMax;
    private Double tempMin;
    private Double precipitation;
}
