package com.vitkvsk.kafka_redis_postgre.coin_registry.IT;

import static org.assertj.core.api.Assertions.*;
import com.vitkvsk.kafka_redis_postgre.coin_registry.Coin;
import com.vitkvsk.kafka_redis_postgre.coin_registry.CoinRepository;
import com.vitkvsk.kafka_redis_postgre.coin_registry.CoinService;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
public class CoinServiceIT {

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private CoinService coinService;

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @DynamicPropertySource
    static  void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldReturnCoinAfterSave() {

        String symbol = "BTC";
        String name = "Bitcoin";

        Either<String, Coin> result = coinService.registerCoin(symbol, name);

        assertThat(result.isRight()).isTrue();
    }

    @Test
    void shouldReturnLeft_WhenCoinAlreadyExist() {
        String symbol = "ETH";
        String name = "ethereum";
        Coin coin = new Coin(symbol, name);

        coinRepository.save(coin);

        Either<String, Coin> result = coinService.registerCoin("ETH", "ethereum");

        assertThat(result.isLeft()).isTrue();
    }
}
