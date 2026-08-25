package com.kanapa4.travel_brief.service;

import com.kanapa4.travel_brief.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CountryDataExtractor {

    public CountryResponse extractCountry(RestCountriesResponse response) {
        return Optional.ofNullable(response)
                .map(RestCountriesResponse::getData)
                .map(RestCountriesResponse.DataWrapper::getObjects)
                .filter(objects -> !CollectionUtils.isEmpty(objects))
                .map(List::getFirst)
                .orElse(null);
    }

    public String extractCountryName(CountryResponse countryData, String fallback) {
        return Optional.ofNullable(countryData)
                .map(CountryResponse::getNames)
                .map(Names::getCommon)
                .orElse(fallback);
    }

    public String extractCapital(CountryResponse countryData) {
        return Optional.ofNullable(countryData)
                .map(CountryResponse::getCapitals)
                .filter(capitals -> !CollectionUtils.isEmpty(capitals))
                .map(List::getFirst)
                .map(Capital::getName)
                .orElse("Unknown");
    }

    public CurrencyDto extractCurrency(CountryResponse countryData) {
        return Optional.ofNullable(countryData)
                .map(CountryResponse::getCurrencies)
                .filter(currencies -> !CollectionUtils.isEmpty(currencies))
                .map(List::getFirst)
                .map(first -> new CurrencyDto(first.getCode(), first.getName()))
                .orElse(null);
    }

    public Coordinates extractCapitalCoordinates(CountryResponse countryData) {
        return Optional.ofNullable(countryData)
                .map(CountryResponse::getCapitals)
                .filter(capitals -> !CollectionUtils.isEmpty(capitals))
                .map(List::getFirst)
                .map(Capital::getCoordinates)
                .filter(coords -> coords.getLat() != null && coords.getLng() != null)
                .orElse(null);
    }
}
