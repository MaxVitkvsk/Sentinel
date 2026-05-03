package com.vitkvsk.kafka_redis_postgre.price_ingestion.infrastructure;


import com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure.CoinPriceEvent;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceIngestionService;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class PriceConsumer {

    private final PriceIngestionService priceIngestionService;
    private final PriceQueryService priceQueryService;

    @KafkaListener(topics = "coin-prices", groupId = "sentinel-group")
    public void consumePrice(CoinPriceEvent event) {
        log.info("Received price event from Kafka: {}", event);

        saveToHistory(event);

        priceQueryService.updatePriceCache(event.symbol(), event.price());
    }

    private void saveToHistory(CoinPriceEvent event) {
        priceIngestionService.ingestPrice(event.symbol(), event.price())
                .peek(history -> log.info("Saved to DB: {}", history.getId()))
                .peekLeft(error -> log.error("DB Save Error: {}", error));
    }
}
