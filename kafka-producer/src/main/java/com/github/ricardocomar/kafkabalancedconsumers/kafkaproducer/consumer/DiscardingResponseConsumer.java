package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.config.AppProperties;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.model.MessageEvent;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Component
@Profile("!group")
public class DiscardingResponseConsumer {
	
	@Autowired
	private AppProperties appProps;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DiscardingResponseConsumer.class);
	
	@Autowired
	private ApplicationContext appContext;

	@KafkaListener(topics = "topicOutbound", groupId = "producerGroup-${random.value}")
	public void consumeResponse(@Payload final ResponseMessage message) {

		LOGGER.info("Received Message: {}", message);
		
		if (!appProps.getInstanceId().equals(message.getOrigin())) {
			LOGGER.warn("Not mine... discarding !");
			return;
		}
		
		appContext.publishEvent(new MessageEvent(message));
		
	}
}
