package com.kanapa4.travel_brief.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BudgetDto {
    private BigDecimal pln;
    private BigDecimal local;
    private BigDecimal rate;
}
