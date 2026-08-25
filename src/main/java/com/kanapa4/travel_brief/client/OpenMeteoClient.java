package com.kanapa4.travel_brief.client;

import com.kanapa4.travel_brief.client.fallback.OpenMeteoClientFallback;
import com.kanapa4.travel_brief.dto.WeatherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "open-meteo", fallback = OpenMeteoClientFallback.class)
public interface OpenMeteoClient {

    @GetMapping("/forecast")
    WeatherResponse getForecast(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("daily") String daily,
            @RequestParam("forecast_days") int forecastDays,
            @RequestParam("timezone") String timezone
    );
}
