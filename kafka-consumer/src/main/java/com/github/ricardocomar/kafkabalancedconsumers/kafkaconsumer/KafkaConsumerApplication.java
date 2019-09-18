package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.config.AppProperties;

@EnableKafka
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(AppProperties.class)
public class KafkaConsumerApplication implements CommandLineRunner {

	public static void main(final String[] args) {
		SpringApplication.run(KafkaConsumerApplication.class);
	}

	@Override
	public void run(final String... args) throws Exception {
	}

}
