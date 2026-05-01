package com.vitkvsk.kafka_redis_postgre.coin_registry;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoinService {
    private final CoinRepository coinRepository;

    @Transactional
    public Either<String, Coin> registerCoin(String symbol, String name) {
        return checkAvailability(symbol)
                .map(s -> {
                    Coin coin = new Coin(symbol, name);
                    return coinRepository.save(coin);
                });
    }

    private Either<String, String> checkAvailability(String symbol) {
        return coinRepository.existsBySymbol(symbol.toUpperCase())
                ? Either.left("We have this coin in DB")
                : Either.right(symbol);
    }

}
