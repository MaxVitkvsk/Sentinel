package com.vitkvsk.kafka_redis_postgre.price_ingestion.IT;

import static org.assertj.core.api.Assertions.assertThat;

import com.vitkvsk.kafka_redis_postgre.AbstractIntegrationTest;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.math.BigDecimal;
import java.util.Objects;

@Tag("integration")
@SpringBootTest
@Testcontainers
public class PriceQueryServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PriceQueryService priceQueryService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        Objects.requireNonNull(cacheManager.getCache("coinPrices")).clear();
    }

    @Test
    @DisplayName("Должен возвращать цену из кэша после её обновления")
    void shouldReturnPriceFromCacheAfterUpdate() {
        String symbol = "BTC";
        BigDecimal amount = new BigDecimal("75000.00");

        priceQueryService.updatePriceCache(symbol, amount);
        String cachedAmount = priceQueryService.getCachedAmount(symbol);

        assertThat(cachedAmount).isEqualTo("75000.00");
    }

    @Test
    @DisplayName("Должен возвращать null, если в кэше ничего нет (Cache Miss)")
    void shouldReturnNullWhenCacheIsEmpty() {
        String result = priceQueryService.getCachedAmount("ETH");

        assertThat(result).isNull();
    }
}