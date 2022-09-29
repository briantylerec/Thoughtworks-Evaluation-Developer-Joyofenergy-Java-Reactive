package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects; 
import java.util.stream.Collectors;

@Service
public class PricePlanService {

    private final Flux<List<PricePlan>> pricePlans;
    private final MeterReadingService meterReadingService;

    public PricePlanService(Flux<List<PricePlan>> pricePlans, MeterReadingService meterReadingService) {
        this.pricePlans = pricePlans;
        this.meterReadingService = meterReadingService;
    }

    public PricePlan getPlanById(String planId){
        return pricePlans.blockFirst().stream()
                .filter(plan -> Objects.equals(plan.getPlanName(), planId))
                .collect(Collectors.toList()).get(0);
    }

    public Flux<Map<String, BigDecimal>> getConsumptionCostOfElectricityReadingsForEachPricePlan(String smartMeterId) {
        Flux<List<ElectricityReading>> electricityReadings = meterReadingService.getReadings(smartMeterId);
        if (electricityReadings.count().blockOptional().get() == 0 ) {
            return Flux.empty();
        }
        Map<String, BigDecimal> result = pricePlans.blockFirst().stream().collect(
            Collectors.toMap(PricePlan::getPlanName, t -> calculateCost(electricityReadings, t)));
        return Flux.just(result);
    }

    public BigDecimal calculateCost(Flux<List<ElectricityReading>> electricityReadings, PricePlan pricePlan) {
        BigDecimal average = calculateAverageReading(electricityReadings);
        BigDecimal timeElapsed = calculateTimeElapsed(electricityReadings);

        BigDecimal averagedCost = average.divide(timeElapsed, RoundingMode.HALF_UP);
        return averagedCost.multiply(pricePlan.getUnitRate());
    }

    private BigDecimal calculateAverageReading(Flux<List<ElectricityReading>> electricityReadings) { 
        BigDecimal summedReadings = electricityReadings.blockFirst().stream()
                .map(ElectricityReading::getReading)
                .reduce(BigDecimal.ZERO, (reading, accumulator) -> reading.add(accumulator));

        return summedReadings.divide(BigDecimal.valueOf(electricityReadings.blockFirst().size()), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTimeElapsed(Flux<List<ElectricityReading>> electricityReadings) {
        ElectricityReading first = electricityReadings.blockFirst().stream()
                .min(Comparator.comparing(ElectricityReading::getTime))
                .get();
        ElectricityReading last = electricityReadings.blockFirst().stream()
                .max(Comparator.comparing(ElectricityReading::getTime))
                .get();

        return BigDecimal.valueOf(Duration.between(first.getTime(), last.getTime()).getSeconds() / 3600.0);
    }

}
