package com.kanapa4.travel_brief.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CountryNotFoundException extends RuntimeException {
    public CountryNotFoundException(String country) {
        super("Country not found: " + country + " (fallback triggered — external API unavailable)");
    }
}
