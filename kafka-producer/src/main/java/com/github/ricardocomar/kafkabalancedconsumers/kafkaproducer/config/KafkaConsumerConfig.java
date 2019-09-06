package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.config;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

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

	@Configuration
	@Profile("!group")
	public static class ConcurrentContainerFactoryConfiguration {

		@Autowired @Qualifier("instanceId")
		private String instanceId;
		
		@Bean 
		public ConcurrentKafkaListenerContainerFactory<String, ResponseMessage> kafkaListenerContainerFactory(
				@Value("${kafkaProducer.consumer.containerFactory.concurrency}") Integer concurrency,
				@Value("${kafkaProducer.consumer.containerFactory.properties.poolTimeout}") Integer poolTimeout,
				ConsumerFactory<String, Object> consumerFactory) {
			ConcurrentKafkaListenerContainerFactory<String, ResponseMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
			factory.setConsumerFactory(consumerFactory);
			factory.setConcurrency(concurrency);
			factory.getContainerProperties().setPollTimeout(poolTimeout);
			factory.setRecordFilterStrategy(
				record -> !instanceId.equals(record.value().getOrigin()));
			return factory;
		}

	}


	@Configuration
	@Profile("group")
	public static class GroupContainerFactoryConfiguration {

		@Autowired @Qualifier("instanceId")
		private String instanceId;

		@Bean
		public ConcurrentKafkaListenerContainerFactory<String, ResponseMessage> kafkaListenerContainerFactory(
			@Value("${kafkaProducer.consumer.containerFactory.concurrency}") Integer concurrency,
			@Value("${kafkaProducer.consumer.containerFactory.properties.poolTimeout}") Integer poolTimeout,
			ConsumerFactory<String, Object> consumerFactory) {
			ConcurrentKafkaListenerContainerFactory<String, ResponseMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
			factory.setConsumerFactory(consumerFactory);
			factory.setConcurrency(concurrency);
			factory.getContainerProperties().setPollTimeout(poolTimeout);
				return factory;
		}
	}
}
