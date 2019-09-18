package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.consumer;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.producer.ReturnProducer;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.service.MessageProcessor;
import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Component
@KafkaListener(topics = "topicInbound")
public class MessageConsumer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);

	@Autowired
	private MessageProcessor processor;
	
	@Autowired
	private ReturnProducer producer;

	@KafkaHandler
	@Transactional(value = TxType.SUPPORTS)
	public void handle(final RequestMessage message) {

		LOGGER.info("Received Message ({})", message);
		
		final ResponseMessage response = processor.process(message);
		LOGGER.info("Message Processed: ({})", response);
		
		producer.sendMessage(response);
	}
}
