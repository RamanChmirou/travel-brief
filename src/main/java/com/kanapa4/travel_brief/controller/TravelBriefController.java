package com.kanapa4.travel_brief.controller;

import com.kanapa4.travel_brief.dto.TravelBriefResponse;
import com.kanapa4.travel_brief.service.TravelBriefService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TravelBriefController {
    private final TravelBriefService travelBriefService;

    @GetMapping("/travel-brief")
    public TravelBriefResponse getTravelBrief(@RequestParam String country, @RequestParam BigDecimal budgetPln) {
        return travelBriefService.getTravelBrief(country, budgetPln);
    }
}
