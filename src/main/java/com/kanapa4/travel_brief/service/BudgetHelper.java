package com.kanapa4.travel_brief.service;

import com.kanapa4.travel_brief.client.FrankfurterClient;
import com.kanapa4.travel_brief.dto.BudgetDto;
import com.kanapa4.travel_brief.dto.CurrencyDto;
import com.kanapa4.travel_brief.dto.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class BudgetHelper {
    private final FrankfurterClient frankfurterClient;

    public BudgetDto calculateBudget(BigDecimal budgetPln, CurrencyDto currency) {
        BigDecimal rate = fetchExchangeRate(currency);
        if (rate == null) {
            return null;
        }
        BigDecimal localBudget = budgetPln.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return new BudgetDto(budgetPln, localBudget, rate);
    }

    private BigDecimal fetchExchangeRate(CurrencyDto currency) {
        if (currency == null || currency.getCode() == null) {
            return null;
        }
        if ("PLN".equalsIgnoreCase(currency.getCode())) {
            return BigDecimal.ONE;
        }

        ExchangeRateResponse response = frankfurterClient.getExchangeRate("PLN", currency.getCode());
        if (response != null && response.getRates() != null && response.getRates().containsKey(currency.getCode())) {
            return response.getRates().get(currency.getCode());
        }
        return null;
    }
}
