package com.kanapa4.travel_brief.client;

import com.kanapa4.travel_brief.dto.ExchangeRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "frankfurter", url = "${clients.frankfurter.url}")
public interface FrankfurterClient {
    @GetMapping("/latest")
    ExchangeRateResponse getExchangeRate(@RequestParam("from") String from, @RequestParam("to") String to);
}
