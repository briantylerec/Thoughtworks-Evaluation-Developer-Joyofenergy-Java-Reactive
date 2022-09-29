package uk.tw.energy.service;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

import java.util.Map;

@Service
public class AccountService {

    private final Flux<Map<String, String>> smartMeterToPricePlanAccounts;

    public AccountService(Flux<Map<String, String>> smartMeterToPricePlanAccounts) {
        this.smartMeterToPricePlanAccounts = smartMeterToPricePlanAccounts;
    }

    public String getPricePlanIdForSmartMeterId(String smartMeterId) {
        return smartMeterToPricePlanAccounts.blockFirst().get(smartMeterId);
    }
}
