package com.vitkvsk.kafka_redis_postgre.price_ingestion.unit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.vitkvsk.kafka_redis_postgre.coin_registry.Coin;
import com.vitkvsk.kafka_redis_postgre.coin_registry.CoinRepository;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceHistory;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceHistoryController;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceHistoryRepository;
import com.vitkvsk.kafka_redis_postgre.price_ingestion.PriceIngestionService;
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
    private PriceIngestionService priceIngestionService; // Основная зависимость для текущей цены

    @MockitoBean
    private PriceHistoryRepository priceHistoryRepository; // Для метода /history

    @MockitoBean
    private CoinRepository coinRepository; // Для метода /history

    @MockitoBean
    private CacheManager cacheManager; // Заглушка инфраструктуры

    @Test
    @DisplayName("GET /current/{symbol} должен вернуть 200, если цена найдена сервисом")
    void shouldReturn200_WhenPriceExists() throws Exception {
        // GIVEN: Теперь мокаем Option, который возвращает сервис
        BigDecimal expectedAmount = new BigDecimal("70000.50");
        when(priceIngestionService.getCurrentPrice("BTC"))
                .thenReturn(Option.of(expectedAmount));

        // WHEN & THEN
        mockMvc.perform(get("/api/prices/current/BTC"))
                .andExpect(status().isOk())
                .andExpect(content().string("70000.50"));
    }

    @Test
    @DisplayName("GET /current/{symbol} должен вернуть 404 через fold, если сервис вернул None")
    void shouldReturn404_WhenPriceNotFound() throws Exception {
        // GIVEN
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
        // GIVEN
        UUID coinId = UUID.randomUUID();
        Coin btc = new Coin("BTC", "Bitcoin");
        // Предполагаем наличие ID у созданного объекта

        when(coinRepository.findBySymbol("BTC")).thenReturn(Optional.of(btc));

        PriceHistory historyEntry = new PriceHistory(btc.getId(), new BigDecimal("69000"), "USD", null);
        when(priceHistoryRepository.findAllByCoinIdOrderByObservedAtDesc(btc.getId()))
                .thenReturn(List.of(historyEntry));

        // WHEN & THEN
        mockMvc.perform(get("/api/prices/history/BTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(69000))
                .andExpect(jsonPath("$[0].currency").value("USD"));
    }
}