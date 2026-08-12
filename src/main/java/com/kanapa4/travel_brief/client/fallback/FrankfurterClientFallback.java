package com.kanapa4.travel_brief.client.fallback;

import com.kanapa4.travel_brief.client.FrankfurterClient;
import com.kanapa4.travel_brief.dto.ExchangeRateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class FrankfurterClientFallback implements FrankfurterClient {

    @Override
    public ExchangeRateResponse getExchangeRate(String from, String to) {
        log.warn("Frankfurter API is unavailable — returning fallback rate 1.0 for {} -> {}", from, to);
        return ExchangeRateResponse.builder()
                .rates(Map.of(to, BigDecimal.ONE))
                .build();
    }
}
