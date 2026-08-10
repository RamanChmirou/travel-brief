package com.kanapa4.travel_brief.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanapa4.travel_brief.client.OpenMeteoClient;
import com.kanapa4.travel_brief.dto.WeatherResponse;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.EnableWireMock;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@EnableWireMock
public class RetryConfigTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OpenMeteoClient openMeteoClient;

    @Test
    void retry_503ThenSuccess_ReturnsResponseAfterRetry() throws Exception {
        // given
        WeatherResponse expectedResponse = WeatherResponse.builder()
                .daily(WeatherResponse.Daily.builder()
                        .time(List.of("2026-08-04"))
                        .temperatureMax(List.of(25.5))
                        .temperatureMin(List.of(14.0))
                        .precipitationSum(List.of(0.0))
                        .build())
                .build();
        String jsonAsString = objectMapper.writeValueAsString(expectedResponse);

        stubFor(get(urlPathEqualTo("/forecast"))
                .inScenario("retry-scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(503).withBody("Service Unavailable"))
                .willSetStateTo("after-first-failure"));

        stubFor(get(urlPathEqualTo("/forecast"))
                .inScenario("retry-scenario")
                .whenScenarioStateIs("after-first-failure")
                .willReturn(okJson(jsonAsString)));

        WeatherResponse actualResponse = openMeteoClient.getForecast(
                52.52, 13.41,
                "temperature_2m_max,temperature_2m_min,precipitation_sum",
                1, "auto"
        );

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(2, getRequestedFor(urlPathEqualTo("/forecast")));
    }

    @Test
    void retry_503AllAttempts_ThrowsException() {
        stubFor(get(urlPathEqualTo("/forecast"))
                .willReturn(aResponse().withStatus(503).withBody("Service Unavailable")));

        assertThatThrownBy(() -> openMeteoClient.getForecast(
                52.52, 13.41,
                "temperature_2m_max,temperature_2m_min,precipitation_sum",
                1, "auto"
        )).isInstanceOf(FeignException.class);

        verify(3, getRequestedFor(urlPathEqualTo("/forecast")));
    }

    @Test
    void retry_500Error_NotRetried() {
        stubFor(get(urlPathEqualTo("/forecast"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        assertThatThrownBy(() -> openMeteoClient.getForecast(
                52.52, 13.41,
                "temperature_2m_max,temperature_2m_min,precipitation_sum",
                1, "auto"
        )).isInstanceOf(FeignException.class);

        verify(1, getRequestedFor(urlPathEqualTo("/forecast")));
    }

    @Test
    void retry_twо503ThenSuccess_ReturnsResponseAfterSecondRetry() throws Exception {
        WeatherResponse expectedResponse = WeatherResponse.builder()
                .daily(WeatherResponse.Daily.builder()
                        .time(List.of("2026-08-04"))
                        .temperatureMax(List.of(20.0))
                        .temperatureMin(List.of(10.0))
                        .precipitationSum(List.of(1.5))
                        .build())
                .build();
        String jsonAsString = objectMapper.writeValueAsString(expectedResponse);

        stubFor(get(urlPathEqualTo("/forecast"))
                .inScenario("double-retry-scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(503).withBody("Service Unavailable"))
                .willSetStateTo("after-first-failure"));

        stubFor(get(urlPathEqualTo("/forecast"))
                .inScenario("double-retry-scenario")
                .whenScenarioStateIs("after-first-failure")
                .willReturn(aResponse().withStatus(503).withBody("Service Unavailable"))
                .willSetStateTo("after-second-failure"));

        stubFor(get(urlPathEqualTo("/forecast"))
                .inScenario("double-retry-scenario")
                .whenScenarioStateIs("after-second-failure")
                .willReturn(okJson(jsonAsString)));

        WeatherResponse actualResponse = openMeteoClient.getForecast(
                52.52, 13.41,
                "temperature_2m_max,temperature_2m_min,precipitation_sum",
                1, "auto"
        );

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(3, getRequestedFor(urlPathEqualTo("/forecast")));
    }
}
