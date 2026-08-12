package com.kanapa4.travel_brief.client.fallback;

import com.kanapa4.travel_brief.client.RestCountriesClient;
import com.kanapa4.travel_brief.exception.CountryNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class RestCountriesClientFallbackFactory implements FallbackFactory<RestCountriesClient> {

    @Override
    public RestCountriesClient create(Throwable cause) {
        return name -> {
            log.error("RestCountries API is unavailable — fallback triggered for country: {}. Cause: {}",
                    name, cause.getMessage(), cause);
            throw new CountryNotFoundException(name);
        };
    }
}
