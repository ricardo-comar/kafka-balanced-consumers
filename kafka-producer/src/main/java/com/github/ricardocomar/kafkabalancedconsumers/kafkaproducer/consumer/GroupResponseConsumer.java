package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.consumer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.config.AppProperties;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model.CrossResponse;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.ConcurrentProcessor;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.model.MessageEvent;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Component
@Profile("group")
public class GroupResponseConsumer implements ResponseConsumer {

	@Autowired
	private ConcurrentProcessor processor;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private UrlValidator urlValidator;

	@Autowired
	private AppProperties appProps;
	
	@Autowired
	private ApplicationContext appContext;

	private static final Logger LOGGER = LoggerFactory.getLogger(GroupResponseConsumer.class);

	@Override
	@KafkaListener(groupId = "producerGroup", topicPartitions = @TopicPartition(topic = "${spring.kafka.consumer.topicName}", partitions = "0"))
	public void consumeResponse(@Payload ResponseMessage message) {

		LOGGER.info("Received Message: {}", message);

		if (appProps.getInstanceId().equals(message.getOrigin())) {

			LOGGER.info("Local message...");
			appContext.publishEvent(new MessageEvent(message));;

		} else {
			if (StringUtils.isNotBlank(message.getOrigin()) && urlValidator.isValid(message.getCallback())) {

				CrossResponse xResp = CrossResponse.builder().sender(appProps.getInstanceId()).response(message)
						.build();

				LOGGER.info("Redirecting to correct owner: {}", message.getOrigin());
				ResponseEntity<String> responseEntity = restTemplate.postForEntity(message.getCallback(), xResp,
						String.class);
				LOGGER.info("Response from owner: {}", responseEntity.getStatusCode());

			} else {
				LOGGER.error("Invalid message content, not able to handle or redirect");
			}
		}

	}
}
