package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.consumer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.URLValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model.CrossResponse;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.ConcurrentProcessor;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Component
@Profile("group")
public class GroupResponseConsumer {

	@Autowired
	private ConcurrentProcessor processor;

	@Value("${kafkaConsummer.instance_id}")
	private String instanceId;

	@Autowired
	private RestTemplate restTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(GroupResponseConsumer.class);

	@KafkaListener(groupId = "producerGroup", topicPartitions = @TopicPartition(topic = "${kafkaConsummer.responseConsumer.consumer.topicName}", partitions = "0"))
	public void consumeResponse(@Payload ResponseMessage message,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {

		LOGGER.info("Received Message ({}) from partition: {}", message, partition);

		if (instanceId.equals(message.getOrigin())) {
		
			LOGGER.info("Local message...");
			processor.notifyResponse(message);
		
		} else if (StringUtils.isNotBlank(message.getOrigin())
				&& UrlValidator.getInstance().isValid(message.getCallback())) {
			
			CrossResponse xResp = CrossResponse.builder().sender(instanceId).response(message).build();

			LOGGER.info("Redirecting to correct owner: {}", message.getOrigin());
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(message.getCallback(), xResp,
					String.class);
			LOGGER.info("Response from owner: {}", responseEntity.getStatusCode());
		
		} else {
			LOGGER.error("Invalid message content, not able to handle or redirect");
		}

	}
}
