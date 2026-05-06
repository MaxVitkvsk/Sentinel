package com.vitkvsk.kafka_redis_postgre.coin_registry.IT;

import static org.assertj.core.api.Assertions.*;

import com.vitkvsk.kafka_redis_postgre.AbstractIntegrationTest;
import com.vitkvsk.kafka_redis_postgre.coin_registry.Coin;
import com.vitkvsk.kafka_redis_postgre.coin_registry.CoinRepository;
import com.vitkvsk.kafka_redis_postgre.coin_registry.CoinService;
import io.vavr.control.Either;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Tag("integration")
@Testcontainers
@SpringBootTest
public class CoinServiceIT extends AbstractIntegrationTest {

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private CoinService coinService;


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
