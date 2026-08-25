package com.kanapa4.travel_brief.service;

import com.kanapa4.travel_brief.client.OpenMeteoClient;
import com.kanapa4.travel_brief.dto.Coordinates;
import com.kanapa4.travel_brief.dto.ForecastDto;
import com.kanapa4.travel_brief.dto.WeatherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WeatherHelper {
    private final OpenMeteoClient openMeteoClient;

    public WeatherResponse fetchForecast(Coordinates coords) {
        if (coords == null) {
            return null;
        }
        return openMeteoClient.getForecast(
                coords.getLat(), coords.getLng(),
                "temperature_2m_max,temperature_2m_min,precipitation_sum",
                3, "auto"
        );
    }

    public List<ForecastDto> buildForecastList(WeatherResponse weather) {
        if (weather == null || weather.getDaily() == null || weather.getDaily().getTime() == null) {
            return null;
        }

        WeatherResponse.Daily daily = weather.getDaily();
        List<ForecastDto> forecastList = new ArrayList<>();
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

    public BigDecimal calculateAvgTempMax(WeatherResponse weather) {
        if (weather == null || weather.getDaily() == null || weather.getDaily().getTemperatureMax() == null) {
            return null;
        }
        double avg = weather.getDaily().getTemperatureMax().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        return BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
    }

    public List<String> buildPackingAdvice(WeatherResponse weather) {
        if (weather == null || weather.getDaily() == null || weather.getDaily().getTemperatureMax() == null) {
            return List.of();
        }

        WeatherResponse.Daily daily = weather.getDaily();
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
}
