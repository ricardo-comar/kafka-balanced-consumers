package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.model.ResponseMessage;

//@Configuration
public class KafkaProducerConfig {
//
//	@Value(value = "${kafka.bootstrapAddress}")
//	private String bootstrapAddress;
//
//	@Bean
//	public ProducerFactory<String, ResponseMessage> producerFactory() {
//		Map<String, Object> configProps = new HashMap<>();
//		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
//				bootstrapAddress);
//		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
//				StringSerializer.class);
//		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//				StringSerializer.class);
//		return new DefaultKafkaProducerFactory<>(configProps);
//	}
//
//	@Bean
//	public KafkaTemplate<String, ResponseMessage> kafkaTemplate(
//			@Autowired ProducerFactory<String, ResponseMessage> producerFactory) {
//		return new KafkaTemplate<>(producerFactory);
//	}
}