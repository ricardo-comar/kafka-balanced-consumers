package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.ConcurrentProcessorTest;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Component
public class ResponseConsumer {
	
	@Autowired
	private ConcurrentProcessorTest processor;
	
	@Value("${kafkaConsummer.instance_id}")
	private String instanceId;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseConsumer.class);

	@KafkaListener(topicPartitions = @TopicPartition(topic = "${kafkaConsummer.responseConsumer.consumer.topicName}", partitions="0"))
	public void consumeResponse(@Payload ResponseMessage message,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {

		LOGGER.info("Received Message ({}) from partition: {}", message, partition);
		
		processor.notifyResponse(message);
		
	}
}
