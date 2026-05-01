package com.vitkvsk.kafka_redis_postgre.coin_registry;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coins")
@RequiredArgsConstructor
public class CoinController {
    private final CoinService coinService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody RegisterCoinRequest request) {
        return coinService.registerCoin(request.symbol(), request.name()).fold(
                error -> ResponseEntity.status(HttpStatus.CONFLICT).body(error),
                coin -> ResponseEntity.status(HttpStatus.CREATED).body(coin)
        );
    }

    public record RegisterCoinRequest(String symbol, String name) {}
}
