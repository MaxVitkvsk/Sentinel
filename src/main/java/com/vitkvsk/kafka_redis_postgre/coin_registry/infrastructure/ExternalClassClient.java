package com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure;

import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExternalClassClient {
    private final RestTemplate restTemplate;

    @Value("${coingecko.api.url}")
    private String baseUrl;

    @Value("${coingecko.api.key}")
    private String apiKey;

    public Either<String, BigDecimal> fetchPrice(String externalId, String name, String symbol) {
        return Try.of(() -> {
                    String url = UriComponentsBuilder.fromUriString(baseUrl)
                            .path("/simple/price")
                            .queryParam("vs_currencies", "usd")
                            .queryParam("ids", externalId)
                            .queryParam("names", name)
                            .queryParam("symbols", symbol)
                            .toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-cg-demo-api-key", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            return Option.of(response.getBody())
                    .map(res -> (Map<?, ?>) res.get(externalId))
                    .map(inner -> inner.get("usd"))
                    .map(val -> new BigDecimal(val.toString()))
                    .getOrElseThrow(() -> new RuntimeException("Coin or price not found in API response"));
        }).toEither()
                .mapLeft(Throwable::getMessage);
    }
}
