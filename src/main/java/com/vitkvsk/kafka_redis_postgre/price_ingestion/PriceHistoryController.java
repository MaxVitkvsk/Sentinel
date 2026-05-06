package com.vitkvsk.kafka_redis_postgre.price_ingestion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        return priceIngestionService.findPriceHistory(symbol)
                .fold(
                        error -> ResponseEntity.status(404).body(error),
                        ResponseEntity::ok
                );
    }
}
