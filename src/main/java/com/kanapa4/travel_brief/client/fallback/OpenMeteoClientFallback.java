package com.kanapa4.travel_brief.client.fallback;

import com.kanapa4.travel_brief.client.OpenMeteoClient;
import com.kanapa4.travel_brief.dto.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OpenMeteoClientFallback implements OpenMeteoClient {

    @Override
    public WeatherResponse getForecast(double latitude, double longitude,
                                       String daily, int forecastDays, String timezone) {
        log.warn("OpenMeteo API is unavailable — returning fallback weather for [{}, {}]", latitude, longitude);
        return WeatherResponse.builder()
                .daily(WeatherResponse.Daily.builder()
                        .time(List.of("unknown"))
                        .temperatureMax(List.of(42.0))
                        .temperatureMin(List.of(0.0))
                        .precipitationSum(List.of(0.0))
                        .build())
                .build();
    }
}
