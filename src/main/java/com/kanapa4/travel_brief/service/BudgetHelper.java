package com.kanapa4.travel_brief.service;

import com.kanapa4.travel_brief.client.FrankfurterClient;
import com.kanapa4.travel_brief.dto.BudgetDto;
import com.kanapa4.travel_brief.dto.CurrencyDto;
import com.kanapa4.travel_brief.dto.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BudgetHelper {
    private final FrankfurterClient frankfurterClient;

    public BudgetDto calculateBudget(BigDecimal budgetPln, CurrencyDto currency) {
        return Optional.ofNullable(fetchExchangeRate(currency))
                .map(rate -> {
                    BigDecimal localBudget = budgetPln.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                    return new BudgetDto(budgetPln, localBudget, rate);
                })
                .orElse(null);
    }

    private BigDecimal fetchExchangeRate(CurrencyDto currency) {
        return Optional.ofNullable(currency)
                .map(CurrencyDto::getCode)
                .map(code -> {
                    if ("PLN".equalsIgnoreCase(code)) {
                        return BigDecimal.ONE;
                    }
                    ExchangeRateResponse response = frankfurterClient.getExchangeRate("PLN", code);
                    return Optional.ofNullable(response)
                            .map(ExchangeRateResponse::getRates)
                            .map(rates -> rates.get(code))
                            .orElse(null);
                })
                .orElse(null);
    }
}
