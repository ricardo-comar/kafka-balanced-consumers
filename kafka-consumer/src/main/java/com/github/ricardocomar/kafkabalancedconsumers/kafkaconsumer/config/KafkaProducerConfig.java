package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.config;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Configuration
public class KafkaProducerConfig {
	@Bean @Lazy
	public ProducerFactory<String, ResponseMessage> producerFactory(
			@Autowired KafkaProperties kafkaProps) {
		return new DefaultKafkaProducerFactory<String, ResponseMessage>(kafkaProps.buildProducerProperties(),
				new StringSerializer(), new JsonSerializer<ResponseMessage>());
	}

	@Bean @Lazy
	public KafkaTemplate<String, ResponseMessage> kafkaTemplate(
			@Autowired ProducerFactory<String, ResponseMessage> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}
}