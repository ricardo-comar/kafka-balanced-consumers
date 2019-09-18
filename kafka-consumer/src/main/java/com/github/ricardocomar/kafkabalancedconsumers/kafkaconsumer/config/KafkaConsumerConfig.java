package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;

@Configuration
public class KafkaConsumerConfig {

	@Autowired
	private AppProperties appProps;

	@Bean @Lazy
	public ConsumerFactory<String, Object> consumerFactory(@Autowired final KafkaProperties kafkaProps) {
		final JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
		jsonDeserializer.addTrustedPackages("*");
		return new DefaultKafkaConsumerFactory<>(kafkaProps.buildConsumerProperties(), new StringDeserializer(),
				jsonDeserializer);
	}

	@Bean @Lazy
	public ConcurrentKafkaListenerContainerFactory<String, RequestMessage> kafkaListenerContainerFactory(
			final ConsumerFactory<String, Object> consumerFactory) {
		final ConcurrentKafkaListenerContainerFactory<String, RequestMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setRecordInterceptor(recordInterceptor());
		factory.setConcurrency(appProps.getConsumer().getContainerFactory().getConcurrency());
		factory.getContainerProperties()
				.setPollTimeout(appProps.getConsumer().getContainerFactory().getProperties().getPoolTimeout());
		return factory;
	}

	private RecordInterceptor<String, RequestMessage> recordInterceptor() {

		return new RecordInterceptor<String, RequestMessage>() {

			@Override
			public ConsumerRecord<String, RequestMessage> intercept(
					final ConsumerRecord<String, RequestMessage> record) {

				record.headers().headers(KafkaHeaders.CORRELATION_ID).forEach((h) -> {
					MDC.put(AppProperties.PROP_CORRELATION_ID, new String(h.value()));
				});
				return record;
			}
		};
	}

}
