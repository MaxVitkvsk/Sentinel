package com.vitkvsk.kafka_redis_postgre;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Tag("integration")
@SpringBootTest
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));
    static final RedisContainer REDIS = new RedisContainer(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    static {
        Startables.deepStart(POSTGRES, KAFKA, REDIS).join();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);

        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }
}
