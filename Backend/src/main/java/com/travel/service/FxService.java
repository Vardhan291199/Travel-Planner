package com.travel.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class FxService {
    private final ExchangeRateApiService rates;
    private final SoapCalculatorService soap;

    public FxService(ExchangeRateApiService rates, SoapCalculatorService soap) {
        this.rates = rates;
        this.soap = soap;
    }

    public Mono<Double> convert(String from, String to, double amount) {
        return rates.getRate(from, to)
                .flatMap(rate -> soap.multiply(amount, rate));
    }
}
