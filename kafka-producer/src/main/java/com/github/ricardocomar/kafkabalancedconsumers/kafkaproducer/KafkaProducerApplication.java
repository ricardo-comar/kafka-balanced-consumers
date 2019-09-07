package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.config.AppProperties;

@EnableKafka
@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class KafkaProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaProducerApplication.class, args);
	}

}
