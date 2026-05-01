package com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure;

import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ExternalClassClient {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${coingecko.api.key}")
    private String apiKey;

    // to do изменить сигнатуру метода, чтобы кроме externalId, передавать name и symbol, для динамической работы с url
    public Either<String, BigDecimal> fetchPrice(String externalId) {
        return Try.of(() -> {
            String url =  "https://api.coingecko.com/api/v3/simple/price?vs_currencies=usd&ids=" + externalId + "&names=Bitcoin&symbols=btc";

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
