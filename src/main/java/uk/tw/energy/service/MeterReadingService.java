package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.tw.energy.domain.ElectricityReading;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; 

@Service
public class MeterReadingService {

    private final Map<String, List<ElectricityReading>> meterAssociatedReadings;

    public MeterReadingService(Map<String, List<ElectricityReading>> meterAssociatedReadings) {
        this.meterAssociatedReadings = meterAssociatedReadings;
    }

    public Flux<List<ElectricityReading>> getReadings(String smartMeterId) {
        List<ElectricityReading>  response = meterAssociatedReadings.get(smartMeterId);
        Flux<List<ElectricityReading>> result= Flux.empty();
        if(!smartMeterId.equals("not-found") && response != null){
            result = Flux.just(response);
        }
        return result;
    }

    public List<ElectricityReading> getReadingsList(String smartMeterId) {
        return meterAssociatedReadings.get(smartMeterId);
    }

    public void storeReadings(String smartMeterId, List<ElectricityReading> electricityReadings) {
        if (!meterAssociatedReadings.containsKey(smartMeterId)) {
            meterAssociatedReadings.put(smartMeterId, new ArrayList<>());
        }
        meterAssociatedReadings.get(smartMeterId).addAll(electricityReadings);
    }
}
