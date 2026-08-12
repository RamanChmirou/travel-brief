package com.kanapa4.travel_brief;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.kanapa4.travel_brief.client.FrankfurterClient;
import com.kanapa4.travel_brief.client.OpenMeteoClient;
import com.kanapa4.travel_brief.client.RestCountriesClient;
import com.kanapa4.travel_brief.dto.*;
import com.kanapa4.travel_brief.exception.CountryNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.EnableWireMock;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableWireMock
@AutoConfigureMockMvc
public class FallbackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestCountriesClient restCountriesClient;

    @Autowired
    private OpenMeteoClient openMeteoClient;

    @Autowired
    private FrankfurterClient frankfurterClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void restCountries_returns500_fallbackThrowsCountryNotFoundException() {
        // given — RestCountries returns 500
        stubFor(WireMock.get(urlPathEqualTo("/"))
                .withQueryParam("q", equalTo("Narnia"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        // when & then — fallback throws CountryNotFoundException
        assertThatThrownBy(() -> restCountriesClient.getCountryByName("Narnia"))
                .isInstanceOf(CountryNotFoundException.class)
                .hasMessageContaining("Narnia")
                .hasMessageContaining("fallback triggered");
    }

    @Test
    void openMeteo_returns500_fallbackReturnsDefaultWeather() {
        // given — OpenMeteo returns 500
        stubFor(WireMock.get(urlPathEqualTo("/forecast"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        // when
        WeatherResponse result = openMeteoClient.getForecast(
                52.52, 13.41,
                "temperature_2m_max,temperature_2m_min,precipitation_sum",
                3, "auto"
        );

        // then — fallback returns dummy data (42°C, date "unknown")
        assertThat(result).isNotNull();
        assertThat(result.getDaily()).isNotNull();
        assertThat(result.getDaily().getTime()).containsExactly("unknown");
        assertThat(result.getDaily().getTemperatureMax()).containsExactly(42.0);
        assertThat(result.getDaily().getTemperatureMin()).containsExactly(0.0);
        assertThat(result.getDaily().getPrecipitationSum()).containsExactly(0.0);
    }

    @Test
    void frankfurter_returns500_fallbackReturnsDefaultRate() {
        // given — Frankfurter returns 500
        stubFor(WireMock.get(urlPathEqualTo("/latest"))
                .withQueryParam("from", equalTo("PLN"))
                .withQueryParam("to", equalTo("EUR"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        // when
        ExchangeRateResponse result = frankfurterClient.getExchangeRate("PLN", "EUR");

        // then — fallback returns rate 1.0
        assertThat(result).isNotNull();
        assertThat(result.getRates()).containsEntry("EUR", BigDecimal.ONE);
    }

    @Test
    void fullFlow_weatherAndCurrencyDown_returnsResponseWithFallbackValues() throws Exception {
        // given — RestCountries works fine
        CountryResponse country = CountryResponse.builder()
                .names(Names.builder().common("Germany").official("Federal Republic of Germany").build())
                .capitals(List.of(
                        Capital.builder()
                                .name("Berlin")
                                .coordinates(Coordinates.builder().lat(52.52).lng(13.405).build())
                                .build()))
                .currencies(List.of(
                        CurrencyInfo.builder().code("EUR").name("Euro").symbol("€").build()))
                .population(83000000L)
                .build();
        RestCountriesResponse restCountriesResponse = RestCountriesResponse.builder()
                .data(RestCountriesResponse.DataWrapper.builder()
                        .objects(List.of(country))
                        .build())
                .build();
        stubFor(WireMock.get(urlPathEqualTo("/"))
                .withQueryParam("q", equalTo("Germany"))
                .willReturn(okJson(objectMapper.writeValueAsString(restCountriesResponse))));

        // given — OpenMeteo is DOWN → fallback returns 42°C
        stubFor(WireMock.get(urlPathEqualTo("/forecast"))
                .willReturn(aResponse().withStatus(500).withBody("Weather service down")));

        // given — Frankfurter is DOWN → fallback returns rate 1.0
        stubFor(WireMock.get(urlPathEqualTo("/latest"))
                .withQueryParam("from", equalTo("PLN"))
                .withQueryParam("to", equalTo("EUR"))
                .willReturn(aResponse().withStatus(500).withBody("Currency service down")));

        // when & then
        mockMvc.perform(get("/api/travel-brief")
                        .param("country", "Germany")
                        .param("budgetPln", "2000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("Germany"))
                .andExpect(jsonPath("$.capital").value("Berlin"))
                .andExpect(jsonPath("$.currency.code").value("EUR"))
                .andExpect(jsonPath("$.budget.rate").value(1))
                .andExpect(jsonPath("$.budget.local").value("2000.00"))
                .andExpect(jsonPath("$.forecast[0].date").value("unknown"))
                .andExpect(jsonPath("$.forecast[0].tempMax").value(42.0))
                .andExpect(jsonPath("$.forecast[0].tempMin").value(0.0))
                .andExpect(jsonPath("$.avgTempMax").value(42.0))
                .andExpect(jsonPath("$.packingAdvice[0]").value("Lekkie ubrania"))
                .andExpect(jsonPath("$.packingAdvice[1]").value("Krem z filtrem"));
    }

    @Test
    void fullFlow_countryApiDown_returns500() throws Exception {
        // given — RestCountries is DOWN → fallback throws CountryNotFoundException
        stubFor(WireMock.get(urlPathEqualTo("/"))
                .withQueryParam("q", equalTo("Atlantis"))
                .willReturn(aResponse().withStatus(500).withBody("Countries service down")));

        // when & then — the exception propagates
        mockMvc.perform(get("/api/travel-brief")
                        .param("country", "Atlantis")
                        .param("budgetPln", "1000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
