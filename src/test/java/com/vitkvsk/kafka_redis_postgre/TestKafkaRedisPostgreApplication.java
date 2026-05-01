package com.vitkvsk.kafka_redis_postgre;

import org.springframework.boot.SpringApplication;

public class TestKafkaRedisPostgreApplication {

	public static void main(String[] args) {
		SpringApplication.from(KafkaRedisPostgreApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
