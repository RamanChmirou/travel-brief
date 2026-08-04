package com.kanapa4.travel_brief.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryResponse {
    @JsonProperty("names")
    private Names names;
    private List<Capital> capitals;
    private List<CurrencyInfo> currencies;
    private Long population;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Names {
        private String common;
        private String official;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Capital {
        private String name;
        private Coordinates coordinates;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coordinates {
        private Double lat;
        private Double lng;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrencyInfo {
        private String code;
        private String name;
        private String symbol;
    }
}
