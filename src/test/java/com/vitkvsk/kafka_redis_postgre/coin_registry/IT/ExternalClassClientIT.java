package com.vitkvsk.kafka_redis_postgre.coin_registry.IT;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.vitkvsk.kafka_redis_postgre.AbstractIntegrationTest;
import com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure.ExternalClassClient;
import com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure.PriceInformer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("integration")
@SpringBootTest(classes = {ExternalClassClient.class, RestTemplate.class})
@ActiveProfiles("test")
public class ExternalClassClientIT extends AbstractIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void wiremockProperties(DynamicPropertyRegistry registry) {
        // В YAML у тебя ${wiremock.base-url}, значит ключ должен быть ТАКИМ:
        registry.add("wiremock.base-url", wireMock::baseUrl);}

    @Autowired
    private ExternalClassClient client;

    @MockitoBean
    private PriceInformer priceInformer;

    @Test
    void shouldFetchReturnWithDynamicParams(){
        wireMock.stubFor(get(urlPathEqualTo("/simple/price"))
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
