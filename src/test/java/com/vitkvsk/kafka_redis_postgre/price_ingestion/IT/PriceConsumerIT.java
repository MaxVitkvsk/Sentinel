package com.vitkvsk.kafka_redis_postgre.price_ingestion.IT;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure.CoinPriceEvent;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceHistory;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceIngestionService;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceQueryService;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;


@SpringBootTest
@Testcontainers
public class PriceConsumerIT {

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private PriceIngestionService priceIngestionService;

    @MockitoBean
    private PriceQueryService priceQueryService;

    @Test
    void shouldProcessPriceEventCorrectly() {

        UUID priceHistoryId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100000");
        String currency = "usd";
        OffsetDateTime observedAt = OffsetDateTime.now();

        CoinPriceEvent coinPriceEvent = new CoinPriceEvent("BTC", new BigDecimal("65000"));

        when(priceIngestionService.ingestPrice(anyString(), any(BigDecimal.class)))
                .thenReturn(Either.right(new PriceHistory(priceHistoryId, amount, currency, observedAt)));

        kafkaTemplate.send("coin-prices", coinPriceEvent);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(priceQueryService).updatePriceCache("BTC", new BigDecimal("65000"));
            verify(priceIngestionService).ingestPrice("BTC", new BigDecimal("65000"));
        });
    }

}
