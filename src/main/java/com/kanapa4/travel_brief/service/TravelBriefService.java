package com.kanapa4.travel_brief.service;

import com.kanapa4.travel_brief.client.RestCountriesClient;
import com.kanapa4.travel_brief.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TravelBriefService {
    private final RestCountriesClient restCountriesClient;
    private final CountryDataExtractor countryDataExtractor;
    private final WeatherHelper weatherHelper;
    private final BudgetHelper budgetHelper;

    public TravelBriefResponse getTravelBrief(String country, BigDecimal budgetPln) {
        CountryResponse countryData = fetchCountryData(country);
        CurrencyDto currency = countryDataExtractor.extractCurrency(countryData);
        Coordinates coords = countryDataExtractor.extractCapitalCoordinates(countryData);
        WeatherResponse weather = weatherHelper.fetchForecast(coords);

        return buildResponse(countryData, country, currency, weather, budgetPln);
    }

    private CountryResponse fetchCountryData(String country) {
        CountryResponse countryData = countryDataExtractor.extractCountry(
                restCountriesClient.getCountryByName(country));
        if (countryData == null) {
            return null;
        }
        return countryData;
    }

    private TravelBriefResponse buildResponse(CountryResponse countryData, String country,
                                               CurrencyDto currency, WeatherResponse weather,
                                               BigDecimal budgetPln) {
        return TravelBriefResponse.builder()
                .country(countryDataExtractor.extractCountryName(countryData, country))
                .capital(countryDataExtractor.extractCapital(countryData))
                .currency(currency)
                .budget(budgetHelper.calculateBudget(budgetPln, currency))
                .forecast(weatherHelper.buildForecastList(weather))
                .packingAdvice(weatherHelper.buildPackingAdvice(weather))
                .avgTempMax(weatherHelper.calculateAvgTempMax(weather))
                .build();
    }
}
