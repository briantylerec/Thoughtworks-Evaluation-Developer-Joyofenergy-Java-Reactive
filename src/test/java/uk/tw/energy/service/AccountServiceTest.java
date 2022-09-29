package uk.tw.energy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AccountServiceTest {

    private static final String PRICE_PLAN_ID = "price-plan-id";
    private static final String SMART_METER_ID = "smart-meter-id";

    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        Map<String, String> smartMeterToPricePlanAccounts = new HashMap<>();
        smartMeterToPricePlanAccounts.put(SMART_METER_ID, PRICE_PLAN_ID);
        Flux<Map<String, String>> smartMeterToPricePlanAccount = Flux.just(smartMeterToPricePlanAccounts);
        accountService = new AccountService(smartMeterToPricePlanAccount);
    }

    @Test
    public void givenTheSmartMeterIdReturnsThePricePlanId() throws Exception {
        assertThat(accountService.getPricePlanIdForSmartMeterId(SMART_METER_ID)).isEqualTo(PRICE_PLAN_ID);
    }
}
