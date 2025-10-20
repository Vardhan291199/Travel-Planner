package com.travel.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ExchangeRateApiService {
    private final WebClient http = WebClient.create("https://v6.exchangerate-api.com/v6");

    @Value("${app.exrate.api-key}")
    private String apiKey;

    public Mono<Double> getRate(String base, String quote) {
        String path = "/" + apiKey + "/latest/" + base.toUpperCase();
        return http.get().uri(path)
                .retrieve().bodyToMono(String.class)
                .map(JSONObject::new)
                .map(j -> {
                    if (!"success".equalsIgnoreCase(j.optString("result"))) {
                        throw new IllegalStateException("ExchangeRate-API error: " + j);
                    }
                    JSONObject rates = j.optJSONObject("conversion_rates");
                    if (rates == null || !rates.has(quote.toUpperCase())) {
                        throw new IllegalStateException("Missing rate for " + quote + " in response: " + j);
                    }
                    return rates.getDouble(quote.toUpperCase());
                });
    }
}
