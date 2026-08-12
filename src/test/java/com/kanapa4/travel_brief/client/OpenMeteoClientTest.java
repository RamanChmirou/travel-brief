package com.kanapa4.travel_brief.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanapa4.travel_brief.TestConfig;
import com.kanapa4.travel_brief.dto.WeatherResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.wiremock.spring.EnableWireMock;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableWireMock
@Import(TestConfig.class)
public class OpenMeteoClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    OpenMeteoClient openMeteoClient;
    @Test
    void getForecast_DataCorrect_ReturnWeatherResponse() throws Exception {
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
                .withQueryParam("latitude", equalTo("52.52"))
                .withQueryParam("longitude", equalTo("13.41"))
                .withQueryParam("daily", equalTo("temperature_2m_max,temperature_2m_min,precipitation_sum"))
                .withQueryParam("forecast_days", equalTo("1"))
                .withQueryParam("timezone", equalTo("auto"))
                .willReturn(okJson(jsonAsString)));

        WeatherResponse actualResponse = openMeteoClient.getForecast(
                52.52,
                13.41,
                "temperature_2m_max,temperature_2m_min,precipitation_sum",
                1,
                "auto"
        );

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }
}
