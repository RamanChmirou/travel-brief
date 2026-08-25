package com.kanapa4.travel_brief.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryResponse {
    @JsonProperty("names")
    private Names names;
    private List<Capital> capitals;
    private List<CurrencyInfo> currencies;
    private Long population;
}