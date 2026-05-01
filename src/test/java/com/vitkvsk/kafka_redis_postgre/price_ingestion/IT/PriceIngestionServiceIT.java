package com.vitkvsk.kafka_redis_postgre.price_ingestion.IT;

import static org.assertj.core.api.Assertions.*;

import com.vitkvsk.kafka_redis_postgre.coin_registry.CoinService;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceHistory;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceIngestionService;
import io.vavr.control.Either;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

@Testcontainers
@SpringBootTest
public class PriceIngestionServiceIT {

    @Autowired
    private CoinService coinService;

    @Autowired
    private PriceIngestionService priceIngestionService;

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @DynamicPropertySource
    static  void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @DisplayName("Should return PriceHistory (Right) when coin exists and amount is valid")
    void shouldReturnPriceHistoryAfterIngestion() {
        String symbol = "BTC";
        BigDecimal amount = new BigDecimal("70000.00");

        coinService.registerCoin(symbol, "Bitcoin");

        Either<String, PriceHistory> result = priceIngestionService.ingestPrice(symbol, amount);

        assertThat(result.isRight()).isTrue();
    }

    @Test
    @DisplayName("Should return Left when coin is not registered in the system")
    void shouldReturnLeft_WhenCoinNotFound() {
        String symbol = "UNKNOWN";
        BigDecimal amount = new BigDecimal("100");

        Either<String, PriceHistory> result = priceIngestionService.ingestPrice(symbol, amount);

        assertThat(result.isLeft()).isTrue();
        result.getLeft();
    }

    @Test
    @DisplayName("Should return Left when amount is negative")
    void shouldReturnLeft_WhenAmountIsNegative() {
        String symbol = "ETH";
        coinService.registerCoin(symbol, "Ethereum");
        BigDecimal negativeAmount = new BigDecimal("-1.0");

        Either<String, PriceHistory> result = priceIngestionService.ingestPrice(symbol, negativeAmount);

        assertThat(result.isLeft()).isTrue();
        result.getLeft();
    }

    @Test
    @DisplayName("Should return Left when amount is null")
    void shouldReturnLeft_WhenAmountIsNull() {
        String symbol = "SOL";

        coinService.registerCoin(symbol, "Solana");

        Either<String, PriceHistory> result = priceIngestionService.ingestPrice(symbol, null);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("amount must be not null");
    }

    @Test
    @DisplayName("Should return Left when symbol is blank")
    void shouldReturnLeft_WhenSymbolIsBlank() {
        BigDecimal amount = new BigDecimal("100");

        Either<String, PriceHistory> result = priceIngestionService.ingestPrice("  ", amount);

        assertThat(result.isLeft()).isTrue();
        result.getLeft();
    }
}