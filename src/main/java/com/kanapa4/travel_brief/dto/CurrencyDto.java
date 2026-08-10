package com.kanapa4.travel_brief.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CurrencyDto {
    private String code;
    private String name;
}
