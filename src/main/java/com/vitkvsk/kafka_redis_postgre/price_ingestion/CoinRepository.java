package com.vitkvsk.kafka_redis_postgre.price_ingestion;

import com.vitkvsk.kafka_redis_postgre.coin_registry.Coin;

import java.util.Optional;

public interface CoinRepository {
    Optional<Coin> findBySymbol(String symbol);
}
