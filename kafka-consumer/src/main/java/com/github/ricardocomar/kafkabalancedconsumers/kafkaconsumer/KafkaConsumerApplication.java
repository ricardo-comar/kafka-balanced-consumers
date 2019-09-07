package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.config.AppProperties;

@EnableKafka
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class KafkaConsumerApplication implements CommandLineRunner {

	public static void main(String[] args) {
		new SpringApplication(KafkaConsumerApplication.class).run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		Thread.currentThread().join();
	}

}
