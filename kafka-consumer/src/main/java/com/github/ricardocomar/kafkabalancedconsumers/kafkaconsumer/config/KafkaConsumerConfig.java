package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.config;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
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

	// @Bean
	// public ConsumerFactory<String, RequestMessage> consumerFactory(
	// @Autowired KafkaProperties kafkaProps) {
	// return new DefaultKafkaConsumerFactory<String, RequestMessage>(
	// kafkaProps.buildProducerProperties(), new StringDeserializer(),
	// new JsonDeserializer<RequestMessage>());
	//
	// }

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
			ConsumerFactory<String, Object> consumerFactory) {
		ConcurrentKafkaListenerContainerFactory<String, RequestMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		return factory;
	}
}
