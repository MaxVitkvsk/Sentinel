package com.vitkvsk.kafka_redis_postgre.price_ingestion;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "coins_price_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "price_history_gen")
    @SequenceGenerator(
            name = "price_history_gen",
            sequenceName = "coins_price_history_id_seq",
            allocationSize = 50
    )
    private Long id;

    @Column(name = "coin_id", nullable = false)
    private UUID coinId;

    @Column(nullable = false, precision = 38, scale = 11)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "observed_at", nullable = false)
    private OffsetDateTime observedAt;

    public PriceHistory(UUID coinId, BigDecimal amount, String currency, OffsetDateTime observedAt) {
        this.coinId = coinId;
        this.amount = amount;
        this.currency = currency;
        this.observedAt = observedAt;
    }
}
