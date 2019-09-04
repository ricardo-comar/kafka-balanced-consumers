package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.producer.ReturnProducer;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.service.MessageProcessor;

@Component
public class MessageConsumer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);

	@Autowired
	private MessageProcessor processor;
	
	@Autowired
	private ReturnProducer producer;

	@KafkaListener(topicPartitions = @TopicPartition(topic = "${spring.kafka.consumer.topicName}", partitions="0"))
	public void listenToParition(@Payload RequestMessage message,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
		
		LOGGER.info("Received Message ({}) from partition: {}", message, partition);
	}
}
