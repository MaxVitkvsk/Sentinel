package com.vitkvsk.kafka_redis_postgre.coin_registry.IT;


import com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure.CoinPriceEvent;
import com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure.ExternalClassClient;
import com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure.PriceInformer;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class PriceInformerIT {

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @MockitoBean
    private ExternalClassClient client;

    @Autowired
    private PriceInformer priceInformer;

    private final List<CoinPriceEvent> consumedEvents = new CopyOnWriteArrayList<>();

    @KafkaListener(topics = "coin-prices", groupId = "test-group")
    void listen(CoinPriceEvent event) {
        consumedEvents.add(event);
    }

    @Test
    void shouldSendCorrectEventToKafka() {
        BigDecimal mockPrice = new BigDecimal("65000.00");

        when(client.fetchPrice("bitcoin", "Bitcoin", "btc"))
                .thenReturn(Either.right(mockPrice));

        priceInformer.fetchAndSend();

        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertThat(consumedEvents).asList().hasSize(1);
                    CoinPriceEvent sentEvent = consumedEvents.get(0);
                    assertThat(sentEvent.symbol()).isEqualTo("BTC");
                    assertThat(sentEvent.price()).isEqualByComparingTo("65000.00");
                });
    }
}
