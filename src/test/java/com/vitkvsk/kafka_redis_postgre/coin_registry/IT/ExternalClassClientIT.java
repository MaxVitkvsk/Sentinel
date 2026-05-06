package com.vitkvsk.kafka_redis_postgre.coin_registry.IT;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure.ExternalClassClient;
import com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure.PriceInformer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("integration")
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@WireMockTest(httpPort = 8080)
@ActiveProfiles("test")
public class ExternalClassClientIT {

    @Autowired
    private ExternalClassClient client;

    @MockitoBean
    private PriceInformer priceInformer;

    @Test
    void shouldFetchReturnWithDynamicParams(){
        stubFor(get(urlPathEqualTo("/simple/price"))
                .withQueryParam("ids", equalTo("bitcoin"))
                .withQueryParam("names", equalTo("Bitcoin"))
                .withQueryParam("symbols", equalTo("btc"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"bitcoin\":{\"usd\":65000.00}}")));

        var result = client.fetchPrice("bitcoin", "Bitcoin", "btc");

        result.peekLeft(System.out::println);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualByComparingTo("65000.00");
    }
}
