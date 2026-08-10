package com.kanapa4.travel_brief.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanapa4.travel_brief.dto.ExchangeRateResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.EnableWireMock;

import java.math.BigDecimal;
import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnableWireMock
public class FrankfurterClientTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private FrankfurterClient frankfurterClient;

    @Test
    void getCountryByName_DataCorrect_ReturnRestCountriesResponse() throws Exception {
        //given
        HashMap<String, BigDecimal> map = new HashMap<>();
        map.put("EUR", new BigDecimal("4.31235"));
        ExchangeRateResponse exchangeRateResponse = ExchangeRateResponse.builder()
                .rates(map)
                .build();
        String jsonAsString = objectMapper.writeValueAsString(exchangeRateResponse);
        stubFor(get(urlPathEqualTo("/latest"))
                .withQueryParam("from", equalTo("EUR"))
                .withQueryParam("to", equalTo("PLN"))
                .willReturn(okJson(jsonAsString)));
        //when
        ExchangeRateResponse result = frankfurterClient.getExchangeRate("EUR", "PLN");
        //then
        assertAll(
                () -> assertEquals(new BigDecimal("4.31235"), result.getRates().get("EUR"))
        );
    }
}
