package com.vitkvsk.kafka_redis_postgre.coin_registry.unit;

import com.vitkvsk.kafka_redis_postgre.coin_registry.Coin;
import com.vitkvsk.kafka_redis_postgre.coin_registry.CoinController;
import com.vitkvsk.kafka_redis_postgre.coin_registry.CoinService;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(CoinController.class)
class CoinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CacheManager cacheManager;

    @MockitoBean
    private CoinService coinService;

    @Test
    void shouldReturnCreated_WhenRegistrationIsSuccessful() throws Exception {
        String symbol = "BTC";
        String name = "Bitcoin";
        Coin btc = new Coin(symbol, name);

        when(coinService.registerCoin(symbol, name))
                .thenReturn(Either.right(btc));


        mockMvc.perform(post("/api/coins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"BTC\", \"name\":\"Bitcoin\"}"))
                .andExpect(status().isCreated()) // Проверяем HTTP статус 201
                .andExpect(jsonPath("$.symbol").value("BTC"))
                .andExpect(jsonPath("$.name").value("Bitcoin"));
    }

    @Test
    void shouldReturnConflict_WhenCoinAlreadyExists() throws Exception {

        when(coinService.registerCoin(anyString(), anyString()))
                .thenReturn(Either.left("Coin already exists"));

        mockMvc.perform(post("/api/coins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"BTC\", \"name\":\"Bitcoin\"}"))
                .andExpect(status().isConflict());

    }
}
