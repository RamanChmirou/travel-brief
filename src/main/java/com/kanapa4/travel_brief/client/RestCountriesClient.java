package com.kanapa4.travel_brief.client;

import com.kanapa4.travel_brief.config.FeignConfig;
import com.kanapa4.travel_brief.dto.RestCountriesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "rest-countries",
        configuration = FeignConfig.class
)
public interface RestCountriesClient {
    @GetMapping
    RestCountriesResponse getCountryByName(@RequestParam("q") String name);
}
