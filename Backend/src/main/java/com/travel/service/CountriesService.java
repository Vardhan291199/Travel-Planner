package com.travel.service;

import com.travel.model.Countries;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class CountriesService {
    private static final String ENDPOINT = "https://countries.trevorblades.com/";
    private final WebClient http;

    public CountriesService(WebClient http) { this.http = http; }

    public Mono<Countries> getCountry(String code) {
        String query = """
            query($code: ID!) {
              country(code: $code) {
                code
                name
                emoji
                capital
                currency
                continent { name }
                languages { name }
              }
            }
        """;
        JSONObject payload = new JSONObject()
                .put("query", query)
                .put("variables", new JSONObject().put("code", code.toUpperCase()));

        return http.post()
                .uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload.toString())
                .retrieve()
                .bodyToMono(String.class)
                .map(JSONObject::new)
                .map(json -> json.getJSONObject("data").getJSONObject("country"))
                .map(c -> new Countries(
                        c.optString("code", ""),
                        c.optString("name", ""),
                        c.optString("emoji", ""),
                        c.optString("capital", ""),
                        c.optString("currency", ""),
                        c.getJSONObject("continent").optString("name", ""),
                        c.getJSONArray("languages").toList().stream()
                                .map(o -> (java.util.Map<?, ?>) o)
                                .map(m -> m.get("name"))
                                .filter(Objects::nonNull)
                                .map(Object::toString)
                                .toList()

                ));
    }
}
