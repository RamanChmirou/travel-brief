package com.kanapa4.travel_brief;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanapa4.travel_brief.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.wiremock.spring.EnableWireMock;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@EnableWireMock
@AutoConfigureMockMvc
public class TravelBriefIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getTravelBrief_DataCorrect_ReturnTravelBriefResponse() throws Exception {
        //given
        TravelBriefResponse response = TravelBriefResponse.builder()
                .country("Japan")
                .capital("Tokyo")
                .currency(CurrencyDto.builder()
                        .code("JPY")
                        .name("Japanese yen")
                        .build())
                .budget(BudgetDto.builder()
                        .pln(new BigDecimal("5000"))
                        .local(new BigDecimal("210460.00"))
                        .rate(new BigDecimal("42.092"))
                        .build())
                .forecast(List.of(
                        ForecastDto.builder()
                                .date("2026-08-05")
                                .tempMax(29.7)
                                .tempMin(21.5)
                                .precipitation(0.0)
                                .build(),
                        ForecastDto.builder()
                                .date("2026-08-06")
                                .tempMax(31.9)
                                .tempMin(23.9)
                                .precipitation(2.0)
                                .build(),
                        ForecastDto.builder()
                                .date("2026-08-07")
                                .tempMax(33.1)
                                .tempMin(25.6)
                                .precipitation(1.6)
                                .build()
                ))
                .packingAdvice(List.of("Lekkie ubrania", "Krem z filtrem"))
                .avgTempMax(new BigDecimal("31.6"))
                .build();

        HashMap<String, BigDecimal> map = new HashMap<>();
        map.put("JPY", new BigDecimal("42.092"));
        ExchangeRateResponse exchangeRateResponse = ExchangeRateResponse.builder()
                .rates(map)
                .build();
        String jsonExchangeRate = objectMapper.writeValueAsString(exchangeRateResponse);
        stubFor(get(urlPathEqualTo("/latest"))
                .withQueryParam("from", equalTo("PLN"))
                .withQueryParam("to", equalTo("JPY"))
                .willReturn(okJson(jsonExchangeRate)));

        WeatherResponse weatherResponse = WeatherResponse.builder()
                .daily(WeatherResponse.Daily.builder()
                        .time(List.of("2026-08-05", "2026-08-06", "2026-08-07"))
                        .temperatureMax(List.of(29.7, 31.9, 33.1))
                        .temperatureMin(List.of(21.5, 23.9, 25.6))
                        .precipitationSum(List.of(0.0, 2.0, 1.6))
                        .build())
                .build();
        String jsonOpenMeteo = objectMapper.writeValueAsString(weatherResponse);
        stubFor(get(urlPathEqualTo("/forecast"))
                .withQueryParam("latitude", equalTo("35.6762"))
                .withQueryParam("longitude", equalTo("139.6503"))
                .withQueryParam("daily", equalTo("temperature_2m_max,temperature_2m_min,precipitation_sum"))
                .withQueryParam("forecast_days", equalTo("3"))
                .withQueryParam("timezone", equalTo("auto"))
                .willReturn(okJson(jsonOpenMeteo)));

        CountryResponse country = CountryResponse.builder()
                .names(CountryResponse.Names.builder()
                        .common("Japan")
                        .official("Japan")
                        .build())
                .capitals(List.of(
                        CountryResponse.Capital.builder()
                                .name("Tokyo")
                                .coordinates(CountryResponse.Coordinates.builder()
                                        .lat(35.6762)
                                        .lng(139.6503)
                                        .build())
                                .build()
                ))
                .currencies(List.of(
                        CountryResponse.CurrencyInfo.builder()
                                .code("JPY")
                                .name("Japanese yen")
                                .symbol("¥")
                                .build()
                ))
                .population(125100000L)
                .build();
        RestCountriesResponse restCountriesResponse = RestCountriesResponse.builder()
                .data(RestCountriesResponse.DataWrapper.builder()
                        .objects(List.of(country))
                        .build())
                .build();
        String jsonRestCountries = objectMapper.writeValueAsString(restCountriesResponse);
        stubFor(get(urlPathEqualTo("/"))
                .withQueryParam("q", equalTo("Japan"))
                .willReturn(okJson(jsonRestCountries)));


        RequestBuilder request = MockMvcRequestBuilders.get("/api/travel-brief?country=Japan&budgetPln=5000")
                .accept(MediaType.APPLICATION_JSON);

        //when & then
        mockMvc.perform(request)
                .andExpect(jsonPath("$.country").value("Japan"))
                .andExpect(jsonPath("$.currency.code").value("JPY"))
                .andExpect(jsonPath("$.budget.local").value("210460.00"))
                .andExpect(jsonPath("$.forecast").isArray())
                .andExpect(jsonPath("$.forecast[1].tempMax").value(31.9));
    }
}
