package com.kanapa4.travel_brief.service;

import com.kanapa4.travel_brief.client.RestCountriesClient;
import com.kanapa4.travel_brief.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelBriefServiceTest {

    @Mock
    private RestCountriesClient restCountriesClient;

    @Mock
    private CountryDataExtractor countryDataExtractor;

    @Mock
    private WeatherHelper weatherHelper;

    @Mock
    private BudgetHelper budgetHelper;

    @InjectMocks
    private TravelBriefService travelBriefService;

    private CountryResponse buildCountryResponse() {
        return CountryResponse.builder()
                .names(Names.builder().common("Japan").official("Japan").build())
                .capitals(List.of(
                        Capital.builder()
                                .name("Tokyo")
                                .coordinates(Coordinates.builder().lat(35.6762).lng(139.6503).build())
                                .build()))
                .currencies(List.of(
                        CurrencyInfo.builder().code("JPY").name("Japanese yen").symbol("¥").build()))
                .population(125100000L)
                .build();
    }

    private RestCountriesResponse buildRestCountriesResponse(CountryResponse country) {
        return RestCountriesResponse.builder()
                .data(RestCountriesResponse.DataWrapper.builder()
                        .objects(List.of(country))
                        .build())
                .build();
    }

    private WeatherResponse buildWeatherResponse() {
        return WeatherResponse.builder()
                .daily(WeatherResponse.Daily.builder()
                        .time(List.of("2026-08-05", "2026-08-06", "2026-08-07"))
                        .temperatureMax(List.of(29.7, 31.9, 33.1))
                        .temperatureMin(List.of(21.5, 23.9, 25.6))
                        .precipitationSum(List.of(0.0, 2.0, 1.6))
                        .build())
                .build();
    }

    @Test
    void getTravelBrief_happyPath_returnsFullResponse() {
        // given
        String country = "Japan";
        BigDecimal budgetPln = new BigDecimal("5000");
        CountryResponse countryData = buildCountryResponse();
        RestCountriesResponse restResponse = buildRestCountriesResponse(countryData);
        CurrencyDto currency = new CurrencyDto("JPY", "Japanese yen");
        Coordinates coords = Coordinates.builder().lat(35.6762).lng(139.6503).build();
        WeatherResponse weather = buildWeatherResponse();
        BudgetDto budget = new BudgetDto(budgetPln, new BigDecimal("210460.00"), new BigDecimal("42.092"));
        List<ForecastDto> forecastList = List.of(
                new ForecastDto("2026-08-05", 29.7, 21.5, 0.0),
                new ForecastDto("2026-08-06", 31.9, 23.9, 2.0),
                new ForecastDto("2026-08-07", 33.1, 25.6, 1.6)
        );
        List<String> packingAdvice = List.of("Lekkie ubrania", "Krem z filtrem");
        BigDecimal avgTemp = new BigDecimal("31.6");

        when(restCountriesClient.getCountryByName(country)).thenReturn(restResponse);
        when(countryDataExtractor.extractCountry(restResponse)).thenReturn(countryData);
        when(countryDataExtractor.extractCurrency(countryData)).thenReturn(currency);
        when(countryDataExtractor.extractCapitalCoordinates(countryData)).thenReturn(coords);
        when(countryDataExtractor.extractCountryName(countryData, country)).thenReturn("Japan");
        when(countryDataExtractor.extractCapital(countryData)).thenReturn("Tokyo");
        when(weatherHelper.fetchForecast(coords)).thenReturn(weather);
        when(weatherHelper.buildForecastList(weather)).thenReturn(forecastList);
        when(weatherHelper.buildPackingAdvice(weather)).thenReturn(packingAdvice);
        when(weatherHelper.calculateAvgTempMax(weather)).thenReturn(avgTemp);
        when(budgetHelper.calculateBudget(budgetPln, currency)).thenReturn(budget);

        // when
        TravelBriefResponse result = travelBriefService.getTravelBrief(country, budgetPln);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCountry()).isEqualTo("Japan");
        assertThat(result.getCapital()).isEqualTo("Tokyo");
        assertThat(result.getCurrency()).isEqualTo(currency);
        assertThat(result.getBudget()).isEqualTo(budget);
        assertThat(result.getForecast()).hasSize(3);
        assertThat(result.getPackingAdvice()).containsExactly("Lekkie ubrania", "Krem z filtrem");
        assertThat(result.getAvgTempMax()).isEqualTo(new BigDecimal("31.6"));

        verify(restCountriesClient).getCountryByName(country);
        verify(countryDataExtractor).extractCountry(restResponse);
        verify(weatherHelper).fetchForecast(coords);
        verify(budgetHelper).calculateBudget(budgetPln, currency);
    }

    @Test
    void getTravelBrief_nullCoordinates_weatherIsNull() {
        // given
        String country = "Japan";
        BigDecimal budgetPln = new BigDecimal("5000");
        CountryResponse countryData = buildCountryResponse();
        RestCountriesResponse restResponse = buildRestCountriesResponse(countryData);
        CurrencyDto currency = new CurrencyDto("JPY", "Japanese yen");

        when(restCountriesClient.getCountryByName(country)).thenReturn(restResponse);
        when(countryDataExtractor.extractCountry(restResponse)).thenReturn(countryData);
        when(countryDataExtractor.extractCurrency(countryData)).thenReturn(currency);
        when(countryDataExtractor.extractCapitalCoordinates(countryData)).thenReturn(null);
        when(countryDataExtractor.extractCountryName(countryData, country)).thenReturn("Japan");
        when(countryDataExtractor.extractCapital(countryData)).thenReturn("Tokyo");
        when(weatherHelper.fetchForecast(null)).thenReturn(null);
        when(weatherHelper.buildForecastList(null)).thenReturn(null);
        when(weatherHelper.buildPackingAdvice(null)).thenReturn(List.of());
        when(weatherHelper.calculateAvgTempMax(null)).thenReturn(null);
        when(budgetHelper.calculateBudget(budgetPln, currency)).thenReturn(
                new BudgetDto(budgetPln, new BigDecimal("210460.00"), new BigDecimal("42.092")));

        // when
        TravelBriefResponse result = travelBriefService.getTravelBrief(country, budgetPln);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCountry()).isEqualTo("Japan");
        assertThat(result.getForecast()).isNull();
        assertThat(result.getAvgTempMax()).isNull();
        assertThat(result.getPackingAdvice()).isEmpty();
    }

    @Test
    void getTravelBrief_nullCurrency_budgetIsNull() {
        // given
        String country = "Japan";
        BigDecimal budgetPln = new BigDecimal("5000");
        CountryResponse countryData = buildCountryResponse();
        RestCountriesResponse restResponse = buildRestCountriesResponse(countryData);
        Coordinates coords = Coordinates.builder().lat(35.6762).lng(139.6503).build();
        WeatherResponse weather = buildWeatherResponse();

        when(restCountriesClient.getCountryByName(country)).thenReturn(restResponse);
        when(countryDataExtractor.extractCountry(restResponse)).thenReturn(countryData);
        when(countryDataExtractor.extractCurrency(countryData)).thenReturn(null);
        when(countryDataExtractor.extractCapitalCoordinates(countryData)).thenReturn(coords);
        when(countryDataExtractor.extractCountryName(countryData, country)).thenReturn("Japan");
        when(countryDataExtractor.extractCapital(countryData)).thenReturn("Tokyo");
        when(weatherHelper.fetchForecast(coords)).thenReturn(weather);
        when(weatherHelper.buildForecastList(weather)).thenReturn(List.of());
        when(weatherHelper.buildPackingAdvice(weather)).thenReturn(List.of());
        when(weatherHelper.calculateAvgTempMax(weather)).thenReturn(new BigDecimal("31.6"));
        when(budgetHelper.calculateBudget(budgetPln, null)).thenReturn(null);

        // when
        TravelBriefResponse result = travelBriefService.getTravelBrief(country, budgetPln);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBudget()).isNull();
        verify(budgetHelper).calculateBudget(budgetPln, null);
    }

    @Test
    void getTravelBrief_fallbackCountryName_usesInputCountry() {
        // given
        String country = "xyz";
        BigDecimal budgetPln = new BigDecimal("1000");
        CountryResponse countryData = CountryResponse.builder()
                .names(null)
                .capitals(List.of())
                .currencies(List.of())
                .build();
        RestCountriesResponse restResponse = buildRestCountriesResponse(countryData);

        when(restCountriesClient.getCountryByName(country)).thenReturn(restResponse);
        when(countryDataExtractor.extractCountry(restResponse)).thenReturn(countryData);
        when(countryDataExtractor.extractCurrency(countryData)).thenReturn(null);
        when(countryDataExtractor.extractCapitalCoordinates(countryData)).thenReturn(null);
        when(countryDataExtractor.extractCountryName(countryData, country)).thenReturn("xyz");
        when(countryDataExtractor.extractCapital(countryData)).thenReturn("Unknown");
        when(weatherHelper.fetchForecast(null)).thenReturn(null);
        when(weatherHelper.buildForecastList(null)).thenReturn(null);
        when(weatherHelper.buildPackingAdvice(null)).thenReturn(List.of());
        when(weatherHelper.calculateAvgTempMax(null)).thenReturn(null);
        when(budgetHelper.calculateBudget(budgetPln, null)).thenReturn(null);

        // when
        TravelBriefResponse result = travelBriefService.getTravelBrief(country, budgetPln);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCountry()).isEqualTo("xyz");
        assertThat(result.getCapital()).isEqualTo("Unknown");
        assertThat(result.getCurrency()).isNull();
        assertThat(result.getBudget()).isNull();
        assertThat(result.getForecast()).isNull();
    }

    @Test
    void getTravelBrief_verifyDependencyInteractionOrder() {
        // given
        String country = "Germany";
        BigDecimal budgetPln = new BigDecimal("2000");
        CountryResponse countryData = buildCountryResponse();
        RestCountriesResponse restResponse = buildRestCountriesResponse(countryData);
        CurrencyDto currency = new CurrencyDto("EUR", "Euro");
        Coordinates coords = Coordinates.builder().lat(52.52).lng(13.405).build();
        WeatherResponse weather = buildWeatherResponse();

        when(restCountriesClient.getCountryByName(country)).thenReturn(restResponse);
        when(countryDataExtractor.extractCountry(restResponse)).thenReturn(countryData);
        when(countryDataExtractor.extractCurrency(countryData)).thenReturn(currency);
        when(countryDataExtractor.extractCapitalCoordinates(countryData)).thenReturn(coords);
        when(countryDataExtractor.extractCountryName(countryData, country)).thenReturn("Germany");
        when(countryDataExtractor.extractCapital(countryData)).thenReturn("Berlin");
        when(weatherHelper.fetchForecast(coords)).thenReturn(weather);
        when(weatherHelper.buildForecastList(weather)).thenReturn(List.of());
        when(weatherHelper.buildPackingAdvice(weather)).thenReturn(List.of());
        when(weatherHelper.calculateAvgTempMax(weather)).thenReturn(BigDecimal.ZERO);
        when(budgetHelper.calculateBudget(budgetPln, currency)).thenReturn(
                new BudgetDto(budgetPln, new BigDecimal("460.00"), new BigDecimal("0.23")));

        // when
        travelBriefService.getTravelBrief(country, budgetPln);

        // then — verify that the client is called first, then extractors, then weather, then budget
        var inOrder = inOrder(restCountriesClient, countryDataExtractor, weatherHelper, budgetHelper);
        inOrder.verify(restCountriesClient).getCountryByName(country);
        inOrder.verify(countryDataExtractor).extractCountry(restResponse);
        inOrder.verify(countryDataExtractor).extractCurrency(countryData);
        inOrder.verify(countryDataExtractor).extractCapitalCoordinates(countryData);
        inOrder.verify(weatherHelper).fetchForecast(coords);
    }
}
