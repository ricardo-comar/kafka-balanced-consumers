package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.consumer;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.producer.ReturnProducer;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.service.MessageProcessor;
import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Component
public class MessageConsumer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);

	@Autowired
	private MessageProcessor processor;
	
	@Autowired
	private ReturnProducer producer;

	@KafkaListener(topics = "topicInbound")
	@Transactional(value = TxType.SUPPORTS)
	public void listenToParition(@Payload final RequestMessage message,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) final int partition) {

		LOGGER.info("Received Message ({}) from partition: {}", message, partition);
		
		final ResponseMessage response = processor.process(message);
		LOGGER.info("Message Processed: ({})", response);
		
		producer.sendMessage(response);
		
	}
}
