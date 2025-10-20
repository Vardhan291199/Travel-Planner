package com.travel.service;

import com.travel.model.AutocompleteItem;
import com.travel.model.PlaceItem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
public class GeoapifyService {
    private final WebClient http;

    @Value("${app.geoapify.api-key}")
    private String geoKey;

    public GeoapifyService(WebClient http) {
        this.http = http;
    }

    public Flux<AutocompleteItem> autocomplete(String text, int limit) {
        if (!StringUtils.hasText(text) || text.trim().length() < 2) return Flux.empty();

        String clean = URLDecoder.decode(text, StandardCharsets.UTF_8);

        String uri = UriComponentsBuilder
                .fromHttpUrl("https://api.geoapify.com/v1/geocode/autocomplete")
                .queryParam("text", clean)
                .queryParam("limit", limit)
                .queryParam("apiKey", geoKey)
                .build()
                .encode()
                .toUriString();

        return http.get().uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .map(org.json.JSONObject::new)
                .flatMapMany(json -> {
                    JSONArray features = json.optJSONArray("features");
                    if (features == null) return Flux.empty();
                    return Flux.range(0, features.length())
                            .map(features::getJSONObject)
                            .map(f -> f.optJSONObject("properties"))
                            .map(p -> new AutocompleteItem(
                                    p.optString("formatted", null),
                                    p.optString("city", p.optString("name", null)),
                                    p.optString("country_code", "").toUpperCase(),
                                    p.has("lat") ? p.optDouble("lat") : null,
                                    p.has("lon") ? p.optDouble("lon") : null
                            ));
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Flux.empty();
                });
    }


    public Flux<PlaceItem> places(double lat, double lon, int radius, String type) {
        String categories = type.equals("hotel") ? "accommodation" : "catering";

        String uri = UriComponentsBuilder
                .fromHttpUrl("https://api.geoapify.com/v2/places")
                .queryParam("categories", categories)
                .queryParam("filter", String.format("circle:%s,%s,%s", lon, lat, radius))
                .queryParam("bias",   String.format("proximity:%s,%s", lon, lat))
                .queryParam("limit",  30)
                .queryParam("apiKey", geoKey)
                .build()
                .encode()
                .toUriString();

        return http.get().uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(body -> {
                    JSONObject json = new org.json.JSONObject(body);
                    JSONArray features = json.optJSONArray("features");
                    int count = (features == null) ? 0 : features.length();

                    if (count == 0) return Flux.empty();

                    return Flux.range(0, count)
                            .map(i -> features.getJSONObject(i).optJSONObject("properties"))
                            .map(p -> new PlaceItem(
                                    p.optString("name", null),
                                    p.optString("formatted", null),
                                    p.has("distance") ? p.optDouble("distance") : null,
                                    jsonArrayToList(p.optJSONArray("categories")),
                                    p.has("lat") ? p.optDouble("lat") : null,
                                    p.has("lon") ? p.optDouble("lon") : null,
                                    p.has("rating") ? p.optDouble("rating") : null,
                                    p.has("price_level") ? p.optInt("price_level") : null,
                                    p.optString("website", null),
                                    p.optString("place_id", null)
                            ));
                });
    }

    private static java.util.List<String> jsonArrayToList(org.json.JSONArray arr) {
        if (arr == null) return java.util.List.of();
        return java.util.stream.IntStream.range(0, arr.length())
                .mapToObj(arr::optString).toList();
    }
}
