package com.vitkvsk.kafka_redis_postgre.coin_registry;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoinRepository extends JpaRepository<Coin, UUID>   {

    Optional<Coin> findBySymbol(String symbol);

    boolean existsBySymbol(String symbol);


}
