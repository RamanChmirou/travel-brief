package com.kanapa4.travel_brief.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanapa4.travel_brief.TestConfig;
import com.kanapa4.travel_brief.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.wiremock.spring.EnableWireMock;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableWireMock
@Import(TestConfig.class)
public class RestCountriesClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestCountriesClient restCountriesClient;

    @Test
    void getCountryByName_DataCorrect_ReturnsRestCountriesResponse() throws Exception {
        CountryResponse country = CountryResponse.builder()
                .names(Names.builder()
                        .common("Poland")
                        .official("Republic of Poland")
                        .build())
                .capitals(List.of(
                        Capital.builder()
                                .name("Warsaw")
                                .coordinates(Coordinates.builder()
                                        .lat(52.23)
                                        .lng(21.01)
                                        .build())
                                .build()
                ))
                .currencies(List.of(
                        CurrencyInfo.builder()
                                .code("PLN")
                                .name("Polish złoty")
                                .symbol("zł")
                                .build()
                ))
                .population(37950000L)
                .build();

        RestCountriesResponse expectedResponse = RestCountriesResponse.builder()
                .data(RestCountriesResponse.DataWrapper.builder()
                        .objects(List.of(country))
                        .build())
                .build();

        String jsonAsString = objectMapper.writeValueAsString(expectedResponse);

        stubFor(get(urlPathEqualTo("/"))
                .withQueryParam("q", equalTo("Poland"))
                .willReturn(okJson(jsonAsString)));

        RestCountriesResponse actualResponse = restCountriesClient.getCountryByName("Poland");

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }
}