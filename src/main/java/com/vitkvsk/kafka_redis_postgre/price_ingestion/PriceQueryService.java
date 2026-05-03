package com.vitkvsk.kafka_redis_postgre.price_ingestion;

import com.vitkvsk.kafka_redis_postgre.coin_registry.CoinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceQueryService {

    @Cacheable(value = "coinPrices", key = "#symbol.toUpperCase()", unless = "#result == null")
    public String getCachedAmount(String symbol) {
        return null;
    }

    @CachePut(value = "coinPrices", key = "#symbol.toUpperCase()")
    public String updatePriceCache(String symbol, BigDecimal price) {
        log.info("Updating cache for {} with price {}", symbol, price);
        return price.toString();
    }
}
