package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.ConcurrentProcessor;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Component
@Profile("!group")
public class DiscardingResponseConsumer implements ResponseConsumer {
	
	@Autowired
	private ConcurrentProcessor processor;
	
	@Autowired @Qualifier("instanceId")
	private String instanceId;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DiscardingResponseConsumer.class);

	@Override
	@KafkaListener(
			containerFactory = "kafkaListenerContainerFactory",
			topicPartitions = @TopicPartition(topic = "${kafkaConsummer.responseConsumer.consumer.topicName}", partitions="0"))
	public void consumeResponse(@Payload ResponseMessage message) {

		LOGGER.info("Received Message: {}", message);
		
		if (!instanceId.equals(message.getOrigin())) {
			LOGGER.info("Not mine... discarding !");
			return;
		}
		
		processor.notifyResponse(message);
		
	}
}
