package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Configuration
public class KafkaConsumerConfig {
	
	@Bean
	public ConsumerFactory<String, Object> consumerFactory(
			@Autowired final KafkaProperties kafkaProps) {
		final JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
		jsonDeserializer.addTrustedPackages("*");
		return new DefaultKafkaConsumerFactory<>(
				kafkaProps.buildConsumerProperties(), new StringDeserializer(),
				jsonDeserializer);
	}

	@Configuration
	@Profile("!group")
	public static class ConcurrentContainerFactoryConfiguration {

		@Autowired
		private AppProperties appProps;

		@Bean 
		public ConcurrentKafkaListenerContainerFactory<String, ResponseMessage> kafkaListenerContainerFactory(
				final ConsumerFactory<String, Object> consumerFactory) {
			final ConcurrentKafkaListenerContainerFactory<String, ResponseMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
			factory.setConsumerFactory(consumerFactory);
			factory.setRecordInterceptor(recordInterceptor());
			factory.setConcurrency(appProps.getConsumer().getContainerFactory().getConcurrency());
			factory.getContainerProperties().setPollTimeout(appProps.getConsumer().getContainerFactory().getProperties().getPoolTimeout());
			factory.setRecordFilterStrategy(
				record -> !appProps.getInstanceId().equals(record.value().getOrigin()));
			return factory;
		}

	}


	@Configuration
	@Profile("group")
	public static class GroupContainerFactoryConfiguration {

		@Autowired
		private AppProperties appProps;

		@Bean
		public ConcurrentKafkaListenerContainerFactory<String, ResponseMessage> kafkaListenerContainerFactory(
			final ConsumerFactory<String, Object> consumerFactory) {
			final ConcurrentKafkaListenerContainerFactory<String, ResponseMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
			factory.setConsumerFactory(consumerFactory);
			factory.setRecordInterceptor(recordInterceptor());
			factory.setConcurrency(appProps.getConsumer().getContainerFactory().getConcurrency());
			factory.getContainerProperties().setPollTimeout(appProps.getConsumer().getContainerFactory().getProperties().getPoolTimeout());
				
			return factory;
		}
	}

	private static RecordInterceptor<String, ResponseMessage> recordInterceptor() {

		return new RecordInterceptor<String, ResponseMessage>() {

			@Override
			public ConsumerRecord<String, ResponseMessage> intercept(
					final ConsumerRecord<String, ResponseMessage> record) {

				record.headers().headers(KafkaHeaders.CORRELATION_ID).forEach((h) -> {
					MDC.put(AppProperties.PROP_CORRELATION_ID, new String(h.value()));
				});
				return record;
			}
		};
	}
}
