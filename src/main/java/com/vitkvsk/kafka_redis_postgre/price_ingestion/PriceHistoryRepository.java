package com.vitkvsk.kafka_redis_postgre.price_ingestion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    PriceHistory findFirstByCoinIdOrderByObservedAtDesc(UUID coinId);
    List<PriceHistory> findAllByCoinIdOrderByObservedAtDesc(UUID coinId);
}
