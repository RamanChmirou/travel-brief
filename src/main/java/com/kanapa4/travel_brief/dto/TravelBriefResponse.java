package com.kanapa4.travel_brief.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TravelBriefResponse {
    private String country;
    private String capital;
    private CurrencyDto currency;
    private BudgetDto budget;
    private List<ForecastDto> forecast;
    private List<String> packingAdvice;
    private BigDecimal avgTempMax;
}
