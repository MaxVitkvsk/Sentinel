package com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure;

import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class PriceInformer {
    private final ExternalClassClient externalClassClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedRate = 10000)
    public void fetchAndSend() {
        String extId = "bitcoin";
        String symbol = "BTC";
        externalClassClient.fetchPrice("bitcoin")
                .map(price -> new CoinPriceEvent(symbol, price))
                .peek(event -> {

                    kafkaTemplate.send("coin-prices", event.symbol(), event);
                    log.info("Sent price to Kafka: {}", event);
                })
                .peekLeft(error -> log.error("Failed to fetch price for Kafka: {}", error));
    }
}
