package com.vitkvsk.kafka_redis_postgre.price_ingestion;

import com.vitkvsk.kafka_redis_postgre.coin_registry.Coin;
import com.vitkvsk.kafka_redis_postgre.coin_registry.CoinRepository;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("api/prices")
@RequiredArgsConstructor
public class PriceHistoryController {

    private final PriceIngestionService priceIngestionService;
    private final CoinRepository coinRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @GetMapping("/current/{symbol}")
    public ResponseEntity<?> getCurrentPrice(@PathVariable String symbol) {
        return priceIngestionService.getCurrentPrice(symbol.toUpperCase())
                .fold(
                        () -> ResponseEntity.status(404).body("Price or Coin not found"),
                        ResponseEntity::ok
                );
    }

    @GetMapping("/history/{symbol}")
    public ResponseEntity<?> getHistory(@PathVariable String symbol) {
        return Option.ofOptional(coinRepository.findBySymbol(symbol.toUpperCase()))
                .toEither("Coin not found: " + symbol)
                .map(Coin::getId)
                .map(priceHistoryRepository::findAllByCoinIdOrderByObservedAtDesc)
                .fold(
                        error -> ResponseEntity.status(404).body(error),
                        ResponseEntity::ok
                );
    }
}
