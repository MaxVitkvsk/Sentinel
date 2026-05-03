package com.vitkvsk.kafka_redis_postgre.coin_registry.infrastructure;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;


@Configuration
public class KafkaConfig {

    @Value("${app.kafka.partitions}")
    private int partitions;

    @Value("${app.kafka.replicas}")
    private int replicas;

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties properties) {
        Map<String, Object> config = properties.buildProducerProperties();
        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                new JacksonJsonSerializer<>()
        );
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic coinPriceTopic() {
        return TopicBuilder.name("coin-prices")
                .partitions(3)
                .replicas(3)
                .build();
    }
}
