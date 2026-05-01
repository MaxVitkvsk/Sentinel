package com.vitkvsk.kafka_redis_postgre.price_ingestion;

import com.vitkvsk.kafka_redis_postgre.coin_registry.Coin;
import com.vitkvsk.kafka_redis_postgre.coin_registry.CoinRepository;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PriceIngestionService {
    private final PriceHistoryRepository priceHistoryRepository;
    private final CoinRepository coinRepository;
    private final PriceQueryService priceQueryService;

    public Option<BigDecimal> getCurrentPrice(String symbol) {
        return Option.of(priceQueryService.getCachedAmount(symbol.toUpperCase()))
                .map(BigDecimal::new)
                // 2. Если в кэше пусто, идем в БД через существующую логику
                .orElse(() -> findCoinId(symbol.toUpperCase())
                        .map(priceHistoryRepository::findFirstByCoinIdOrderByObservedAtDesc)
                        .map(PriceHistory::getAmount)
                        .toOption());
    }

    public Either<String, PriceHistory> ingestPrice(String symbol, BigDecimal amount) {
        return findCoinId(symbol)
                .flatMap(coinId ->
                        validateAmount(amount)
                                .map(validateAmount ->
                                        saveToHistory(coinId, validateAmount)));
    }

    private Either<String, UUID> findCoinId(String symbol) {
        if(symbol == null ||  symbol.isBlank()) return Either.left("Symbol is required");
        return Option.ofOptional(coinRepository.findBySymbol(symbol.toUpperCase()))
                .map(Coin::getId)
                .toEither("Coin not registered in system: " + symbol);
    }

    private Either<String, BigDecimal> validateAmount(BigDecimal amount) {
        if(amount == null) return Either.left("amount must be not null");
        return amount.compareTo(BigDecimal.ZERO) < 0
            ? Either.left("amount must be greater than 0")
            : Either.right(amount);
    }

    private PriceHistory saveToHistory(UUID coinId, BigDecimal amount) {
        OffsetDateTime observedAt = OffsetDateTime.now(ZoneOffset.UTC);
        String currency = "USD";

        PriceHistory history = new PriceHistory(coinId, amount, currency, observedAt );
        return priceHistoryRepository.save(history);
    }
}
