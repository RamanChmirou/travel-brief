package com.kanapa4.travel_brief.controller;

import com.kanapa4.travel_brief.dto.*;
import com.kanapa4.travel_brief.service.TravelBriefService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TravelBriefController.class)
class TravelBriefControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TravelBriefService travelBriefService;

    private TravelBriefResponse buildFullResponse() {
        return TravelBriefResponse.builder()
                .country("Japan")
                .capital("Tokyo")
                .currency(CurrencyDto.builder().code("JPY").name("Japanese yen").build())
                .budget(BudgetDto.builder()
                        .pln(new BigDecimal("5000"))
                        .local(new BigDecimal("210460.00"))
                        .rate(new BigDecimal("42.092"))
                        .build())
                .forecast(List.of(
                        ForecastDto.builder()
                                .date("2026-08-05").tempMax(29.7).tempMin(21.5).precipitation(0.0).build(),
                        ForecastDto.builder()
                                .date("2026-08-06").tempMax(31.9).tempMin(23.9).precipitation(2.0).build(),
                        ForecastDto.builder()
                                .date("2026-08-07").tempMax(33.1).tempMin(25.6).precipitation(1.6).build()
                ))
                .packingAdvice(List.of("Lekkie ubrania", "Krem z filtrem"))
                .avgTempMax(new BigDecimal("31.6"))
                .build();
    }

    @Test
    void getTravelBrief_happyPath_returns200WithFullJson() throws Exception {
        // given
        TravelBriefResponse response = buildFullResponse();
        when(travelBriefService.getTravelBrief("Japan", new BigDecimal("5000"))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/travel-brief")
                        .param("country", "Japan")
                        .param("budgetPln", "5000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.country").value("Japan"))
                .andExpect(jsonPath("$.capital").value("Tokyo"))
                .andExpect(jsonPath("$.currency.code").value("JPY"))
                .andExpect(jsonPath("$.currency.name").value("Japanese yen"))
                .andExpect(jsonPath("$.budget.pln").value(5000))
                .andExpect(jsonPath("$.budget.local").value("210460.00"))
                .andExpect(jsonPath("$.budget.rate").value(42.092))
                .andExpect(jsonPath("$.forecast", hasSize(3)))
                .andExpect(jsonPath("$.forecast[0].date").value("2026-08-05"))
                .andExpect(jsonPath("$.forecast[0].tempMax").value(29.7))
                .andExpect(jsonPath("$.forecast[1].tempMin").value(23.9))
                .andExpect(jsonPath("$.forecast[2].precipitation").value(1.6))
                .andExpect(jsonPath("$.packingAdvice", hasSize(2)))
                .andExpect(jsonPath("$.packingAdvice[0]").value("Lekkie ubrania"))
                .andExpect(jsonPath("$.packingAdvice[1]").value("Krem z filtrem"))
                .andExpect(jsonPath("$.avgTempMax").value(31.6));

        verify(travelBriefService).getTravelBrief("Japan", new BigDecimal("5000"));
    }

    @Test
    void getTravelBrief_missingCountryParam_returns400() throws Exception {
        mockMvc.perform(get("/api/travel-brief")
                        .param("budgetPln", "5000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(travelBriefService);
    }

    @Test
    void getTravelBrief_missingBudgetParam_returns400() throws Exception {
        mockMvc.perform(get("/api/travel-brief")
                        .param("country", "Japan")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(travelBriefService);
    }

    @Test
    void getTravelBrief_noParams_returns400() throws Exception {
        mockMvc.perform(get("/api/travel-brief")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(travelBriefService);
    }

    @Test
    void getTravelBrief_nullFieldsInResponse_returnsNullsInJson() throws Exception {
        // given
        TravelBriefResponse response = TravelBriefResponse.builder()
                .country("Unknown")
                .capital("Unknown")
                .currency(null)
                .budget(null)
                .forecast(null)
                .packingAdvice(List.of())
                .avgTempMax(null)
                .build();
        when(travelBriefService.getTravelBrief("xyz", new BigDecimal("1000"))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/travel-brief")
                        .param("country", "xyz")
                        .param("budgetPln", "1000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("Unknown"))
                .andExpect(jsonPath("$.capital").value("Unknown"))
                .andExpect(jsonPath("$.currency").doesNotExist())
                .andExpect(jsonPath("$.budget").doesNotExist())
                .andExpect(jsonPath("$.forecast").doesNotExist())
                .andExpect(jsonPath("$.packingAdvice", hasSize(0)))
                .andExpect(jsonPath("$.avgTempMax").doesNotExist());
    }

    @Test
    void getTravelBrief_serviceThrowsException_propagatesException() throws Exception {
        // given
        when(travelBriefService.getTravelBrief("error", new BigDecimal("1000")))
                .thenThrow(new RuntimeException("External API failure"));

        // when & then — without @ExceptionHandler, the RuntimeException propagates as a ServletException
        org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(get("/api/travel-brief")
                        .param("country", "error")
                        .param("budgetPln", "1000")
                        .accept(MediaType.APPLICATION_JSON))
        );
    }

    @Test
    void getTravelBrief_decimalBudget_passedCorrectly() throws Exception {
        // given
        TravelBriefResponse response = TravelBriefResponse.builder()
                .country("France")
                .capital("Paris")
                .build();
        when(travelBriefService.getTravelBrief("France", new BigDecimal("1500.50"))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/travel-brief")
                        .param("country", "France")
                        .param("budgetPln", "1500.50")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("France"))
                .andExpect(jsonPath("$.capital").value("Paris"));

        verify(travelBriefService).getTravelBrief("France", new BigDecimal("1500.50"));
    }

    @Test
    void getTravelBrief_wrongHttpMethod_returns405() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/travel-brief")
                        .param("country", "Japan")
                        .param("budgetPln", "5000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }
}
