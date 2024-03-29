package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Service
public class ReturnProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReturnProducer.class);

	@Autowired
	private KafkaTemplate<String, ResponseMessage> kafkaTemplate;
	
	@Value("topicOutbound")
	private String topicName;
	
	public void sendMessage(final ResponseMessage message) {
		LOGGER.info("Sending message to topic {}", topicName);
		kafkaTemplate.send(topicName, message);
	}
	
	 
}
