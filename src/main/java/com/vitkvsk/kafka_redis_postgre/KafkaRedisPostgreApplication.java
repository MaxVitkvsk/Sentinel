package com.vitkvsk.kafka_redis_postgre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class KafkaRedisPostgreApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaRedisPostgreApplication.class, args);
	}

}
