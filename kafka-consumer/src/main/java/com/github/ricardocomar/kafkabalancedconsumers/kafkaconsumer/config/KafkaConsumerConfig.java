package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.config;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;

@Configuration
public class KafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, Object> consumerFactory(
			@Autowired KafkaProperties kafkaProps) {
		final JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
		jsonDeserializer.addTrustedPackages("*");
		return new DefaultKafkaConsumerFactory<>(
				kafkaProps.buildConsumerProperties(), new StringDeserializer(),
				jsonDeserializer);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, RequestMessage> kafkaListenerContainerFactory(
			@Value("${kafkaConsumer.consumer.containerFactory.concurrency}") Integer concurrency,
			@Value("${kafkaConsumer.consumer.containerFactory.properties.poolTimeout}") Integer poolTimeout,
			ConsumerFactory<String, Object> consumerFactory) {
		ConcurrentKafkaListenerContainerFactory<String, RequestMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setConcurrency(concurrency);
		factory.getContainerProperties().setPollTimeout(poolTimeout);
		return factory;
	}
}
