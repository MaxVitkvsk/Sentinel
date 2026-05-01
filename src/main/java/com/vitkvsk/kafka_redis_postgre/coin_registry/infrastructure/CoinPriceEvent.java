package com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure;

import java.math.BigDecimal;

public record CoinPriceEvent(String symbol, BigDecimal price) { }
