package com.travel.controller;

import com.travel.model.*;
import com.travel.service.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping("/api")
public class ApiController {
  private final GeoapifyService geo;
  private final CountriesService countries;
  private final FxService fx;

  public ApiController(GeoapifyService geo, CountriesService countries, FxService fx) {
    this.geo = geo;
    this.countries = countries;
    this.fx = fx;
  }

  @GetMapping(value="/autocomplete", produces="application/json")
  public Mono<java.util.List<AutocompleteItem>> autocomplete(
          @RequestParam("text") String text,
          @RequestParam(name="limit", defaultValue="8") int limit) {
    return geo.autocomplete(text, limit).collectList();
  }

  @GetMapping(value="/places", produces="application/json")
  public Mono<java.util.List<PlaceItem>> places(
          @RequestParam("lat") double lat,
          @RequestParam("lon") double lon,
          @RequestParam(name="radius", defaultValue="3000") int radius,
          @RequestParam(name="type", defaultValue="restaurant") String type) {
    return geo.places(lat, lon, radius, type).collectList();
  }

  @GetMapping("/fx/convert")
  public Mono<ConvertResult> convert(@RequestParam("from") String from,
                                     @RequestParam("to") String to,
                                     @RequestParam("amount") double amount) {
    return fx.convert(from, to, amount)
            .map(total -> new ConvertResult(
                    from.toUpperCase(), to.toUpperCase(), amount,
                    amount == 0 ? 0 : total / amount,
                    total
            ));
  }

  @GetMapping("/country")
  public Mono<Countries> country(@RequestParam("code") String code) {
    return countries.getCountry(code);
  }
}
