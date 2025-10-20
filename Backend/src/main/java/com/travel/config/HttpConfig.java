package com.travel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
public class HttpConfig {

  @Bean
  public WebClient webClient() {
    HttpClient http = HttpClient.create()
        .responseTimeout(Duration.ofSeconds(15));
    ExchangeStrategies exch = ExchangeStrategies.builder()
        .codecs(c -> c.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
        .build();
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(http))
        .exchangeStrategies(exch)
        .build();
  }

}
