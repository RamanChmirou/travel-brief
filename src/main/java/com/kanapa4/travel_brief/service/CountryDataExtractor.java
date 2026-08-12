package com.kanapa4.travel_brief.service;

import com.kanapa4.travel_brief.client.RestCountriesClient;
import com.kanapa4.travel_brief.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CountryDataExtractor {
    private final RestCountriesClient restCountriesClient;

    public CountryResponse fetchCountryData(String country) {
        return extractCountry(restCountriesClient.getCountryByName(country));
    }

    public CountryResponse extractCountry(RestCountriesResponse response) {
        if (response != null && response.getData() != null
                && response.getData().getObjects() != null
                && !response.getData().getObjects().isEmpty()) {
            return response.getData().getObjects().getFirst();
        }
        return null;
    }

    public String extractCountryName(CountryResponse countryData, String fallback) {
        if (countryData.getNames() != null && countryData.getNames().getCommon() != null) {
            return countryData.getNames().getCommon();
        }
        return fallback;
    }


    public String extractCapital(CountryResponse countryData) {
        if (countryData.getCapitals() != null && !countryData.getCapitals().isEmpty()) {
            Capital capital = countryData.getCapitals().getFirst();
            if (capital.getName() != null) {
                return capital.getName();
            }
        }
        return "Unknown";
    }

    public CurrencyDto extractCurrency(CountryResponse countryData) {
        if (countryData.getCurrencies() != null && !countryData.getCurrencies().isEmpty()) {
            CurrencyInfo first = countryData.getCurrencies().getFirst();
            if (first != null) {
                return new CurrencyDto(first.getCode(), first.getName());
            }
        }
        return null;
    }

    public Coordinates extractCapitalCoordinates(CountryResponse countryData) {
        if (countryData.getCapitals() == null || countryData.getCapitals().isEmpty()) {
            return null;
        }

        Capital capital = countryData.getCapitals().getFirst();
        if (capital.getCoordinates() == null) {
            return null;
        }

        Coordinates coords = capital.getCoordinates();
        if (coords.getLat() == null || coords.getLng() == null) {
            return null;
        }

        return coords;
    }
}
