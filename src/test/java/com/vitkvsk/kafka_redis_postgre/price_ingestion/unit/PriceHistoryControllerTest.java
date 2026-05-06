package com.vitkvsk.kafka_redis_postgre.price_ingestion.unit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

import com.vitkvsk.kafka_redis_postgre.coin_registry.Coin;
import com.vitkvsk.kafka_redis_postgre.coin_registry.CoinRepository;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceHistory;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceHistoryController;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceHistoryRepository;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceIngestionService;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebMvcTest(PriceHistoryController.class)
class PriceHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceIngestionService priceIngestionService;

    @MockitoBean
    private PriceHistoryRepository priceHistoryRepository;

    @MockitoBean
    private CoinRepository coinRepository;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    @DisplayName("GET /current/{symbol} должен вернуть 200, если цена найдена сервисом")
    void shouldReturn200_WhenPriceExists() throws Exception {
        BigDecimal expectedAmount = new BigDecimal("70000.50");
        when(priceIngestionService.getCurrentPrice("BTC"))
                .thenReturn(Option.of(expectedAmount));

        mockMvc.perform(get("/api/prices/current/BTC"))
                .andExpect(status().isOk())
                .andExpect(content().string("70000.50"));
    }

    @Test
    @DisplayName("GET /current/{symbol} должен вернуть 404 через fold, если сервис вернул None")
    void shouldReturn404_WhenPriceNotFound() throws Exception {
        when(priceIngestionService.getCurrentPrice(anyString()))
                .thenReturn(Option.none());

        // WHEN & THEN
        mockMvc.perform(get("/api/prices/current/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Price or Coin not found"));
    }

    @Test
    @DisplayName("GET /history/{symbol} должен вернуть список историй")
    void shouldReturnHistoryList() throws Exception {
        UUID coinId = UUID.randomUUID();
        Coin btc = new Coin("BTC", "Bitcoin");

        PriceHistory historyEntry = new PriceHistory(btc.getId(), new BigDecimal("69000"), "USD", null);
        when(priceIngestionService.findPriceHistory("BTC")).thenReturn(Either.right(List.of(historyEntry)));


        mockMvc.perform(get("/api/prices/history/BTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(69000))
                .andExpect(jsonPath("$[0].currency").value("USD"));
    }
}