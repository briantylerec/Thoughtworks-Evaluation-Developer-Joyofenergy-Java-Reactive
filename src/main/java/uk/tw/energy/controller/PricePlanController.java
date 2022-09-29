package uk.tw.energy.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.PricePlanService;

@RestController
@RequestMapping("/readings")
public class PricePlanController {

    private final PricePlanService pricePlanService;
    private final MeterReadingService meterReadingService;
    private final AccountService accountService;

    public PricePlanController(PricePlanService pricePlanService, MeterReadingService meterReadingService, AccountService accountService) {
        this.pricePlanService = pricePlanService;
        this.meterReadingService = meterReadingService;
        this.accountService =accountService;
    }

    @GetMapping("getCost/{smartMeterId}")
    public ResponseEntity<Map<String, Object>> getCostbySmartmeter(@PathVariable String smartMeterId) {

        List<ElectricityReading> newReadings = meterReadingService.getReadingsList(smartMeterId).stream()    
            .filter(d->d.getTime().compareTo(Instant.now().minus(7, ChronoUnit.DAYS)) >= 0)
            .collect(Collectors.toList());

        if(newReadings.size()<=1) return ResponseEntity.notFound().build();

        String planId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);

        PricePlan plan = pricePlanService.getPlanById(planId);
        BigDecimal pricePlan = pricePlanService.getPlanById(planId).getUnitRate();
        BigDecimal weekCost = pricePlanService.calculateCost(Flux.just(newReadings), plan);

        Map<String, Object> response = new HashMap<>();
        response.put("smartMeterId", smartMeterId);
        response.put("price-plan-id", planId);
        response.put("price-plan", pricePlan);
        response.put("last-week-cost", weekCost);

        return ResponseEntity.ok(response);
    }   
}