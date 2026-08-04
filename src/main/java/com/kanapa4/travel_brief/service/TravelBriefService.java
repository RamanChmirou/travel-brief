package com.kanapa4.travel_brief.service;

import com.kanapa4.travel_brief.client.FrankfurterClient;
import com.kanapa4.travel_brief.client.OpenMeteoClient;
import com.kanapa4.travel_brief.client.RestCountriesClient;
import com.kanapa4.travel_brief.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelBriefService {

    private final RestCountriesClient restCountriesClient;
    private final OpenMeteoClient openMeteoClient;
    private final FrankfurterClient frankfurterClient;

    public TravelBriefResponse getTravelBrief(String country, BigDecimal budgetPln) {
        CountryResponse countryData = fetchCountryData(country);
        if (countryData == null) {
            return null;
        }

        String capital = extractCapital(countryData);
        CurrencyDto currency = extractCurrency(countryData);

        BigDecimal rate = null;
        if (currency != null && currency.getCode() != null) {
            rate = fetchExchangeRate(currency.getCode());
        }

        WeatherResponse weather = fetchForecast(countryData);

        List<ForecastDto> forecastList = null;
        BigDecimal avgTempMax = null;
        List<String> packingAdvice = List.of();

        if (weather != null && weather.getDaily() != null) {
            forecastList = buildForecastList(weather.getDaily());
            avgTempMax = calculateAvgTempMax(weather.getDaily());
            packingAdvice = buildPackingAdvice(weather.getDaily());
        }

        BudgetDto budget = null;
        if (rate != null) {
            budget = calculateBudget(budgetPln, rate);
        }

        return TravelBriefResponse.builder()
                .country(countryData.getNames() != null ? countryData.getNames().getCommon() : country)
                .capital(capital)
                .currency(currency)
                .budget(budget)
                .forecast(forecastList)
                .packingAdvice(packingAdvice)
                .avgTempMax(avgTempMax)
                .build();
    }

    private CountryResponse fetchCountryData(String country) {
        RestCountriesResponse response = restCountriesClient.getCountryByName(country);
        if (response != null && response.getData() != null
                && response.getData().getObjects() != null
                && !response.getData().getObjects().isEmpty()) {
            return response.getData().getObjects().getFirst();
        }
        return null;
    }

    private String extractCapital(CountryResponse countryData) {
        if (countryData.getCapitals() != null && !countryData.getCapitals().isEmpty()) {
            CountryResponse.Capital capital = countryData.getCapitals().getFirst();
            if (capital.getName() != null) {
                return capital.getName();
            }
        }
        return "Unknown";
    }

    private CurrencyDto extractCurrency(CountryResponse countryData) {
        if (countryData.getCurrencies() != null && !countryData.getCurrencies().isEmpty()) {
            CountryResponse.CurrencyInfo first = countryData.getCurrencies().getFirst();
            if (first != null) {
                return new CurrencyDto(first.getCode(), first.getName());
            }
        }
        return null;
    }

    private WeatherResponse fetchForecast(CountryResponse countryData) {
        if (countryData.getCapitals() == null || countryData.getCapitals().isEmpty()) {
            return null;
        }

        CountryResponse.Capital capital = countryData.getCapitals().getFirst();
        if (capital.getCoordinates() == null) {
            return null;
        }

        CountryResponse.Coordinates coords = capital.getCoordinates();
        if (coords.getLat() == null || coords.getLng() == null) {
            return null;
        }

        return openMeteoClient.getForecast(
                coords.getLat(), coords.getLng(),
                "temperature_2m_max,temperature_2m_min,precipitation_sum",
                3, "auto"
        );
    }

    private BigDecimal fetchExchangeRate(String currencyCode) {
        if ("PLN".equalsIgnoreCase(currencyCode)) {
            return BigDecimal.ONE;
        }

        ExchangeRateResponse response = frankfurterClient.getExchangeRate("PLN", currencyCode);
        if (response != null && response.getRates() != null && response.getRates().containsKey(currencyCode)) {
            return response.getRates().get(currencyCode);
        }

        return null;
    }

    private BudgetDto calculateBudget(BigDecimal budgetPln, BigDecimal rate) {
        BigDecimal localBudget = budgetPln.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return new BudgetDto(budgetPln, localBudget, rate);
    }

    private List<ForecastDto> buildForecastList(WeatherResponse.Daily daily) {
        List<ForecastDto> forecastList = new ArrayList<>();
        if (daily.getTime() == null) {
            return forecastList;
        }

        for (int i = 0; i < daily.getTime().size(); i++) {
            forecastList.add(new ForecastDto(
                    daily.getTime().get(i),
                    daily.getTemperatureMax().get(i),
                    daily.getTemperatureMin().get(i),
                    daily.getPrecipitationSum().get(i)
            ));
        }
        return forecastList;
    }

    private BigDecimal calculateAvgTempMax(WeatherResponse.Daily daily) {
        if (daily.getTemperatureMax() == null) {
            return BigDecimal.ZERO;
        }
        double avg = daily.getTemperatureMax().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        return BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
    }

    private double calculateTotalPrecipitation(WeatherResponse.Daily daily) {
        if (daily.getPrecipitationSum() == null) {
            return 0.0;
        }
        return daily.getPrecipitationSum().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private double calculateTempDifference(WeatherResponse.Daily daily) {
        if (daily.getTemperatureMax() == null || daily.getTemperatureMin() == null) {
            return 0.0;
        }
        double globalMax = daily.getTemperatureMax().stream()
                .mapToDouble(Double::doubleValue)
                .max().orElse(0.0);
        double globalMin = daily.getTemperatureMin().stream()
                .mapToDouble(Double::doubleValue)
                .min().orElse(0.0);
        return globalMax - globalMin;
    }

    private List<String> buildPackingAdvice(WeatherResponse.Daily daily) {
        if (daily.getTemperatureMax() == null) {
            return List.of();
        }

        double avgTempMax = daily.getTemperatureMax().stream()
                .mapToDouble(Double::doubleValue)
                .average().orElse(0.0);
        double totalPrecipitation = calculateTotalPrecipitation(daily);
        double tempDifference = calculateTempDifference(daily);

        List<String> advice = new ArrayList<>();

        if (avgTempMax > 25.0) {
            advice.add("Lekkie ubrania");
            advice.add("Krem z filtrem");
        }
        if (avgTempMax < 10.0) {
            advice.add("Kurtka zimowa");
        }
        if (totalPrecipitation > 5.0) {
            advice.add("Parasol");
        }
        if (tempDifference > 12.0) {
            advice.add("Ubrania warstwowe");
        }

        return advice;
    }
}
