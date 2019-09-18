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
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.config.AppProperties;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model.CrossResponse;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.model.MessageEvent;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Component
@Profile("group")
public class GroupResponseConsumer {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private UrlValidator urlValidator;

	@Autowired
	private AppProperties appProps;
	
	@Autowired
	private ApplicationContext appContext;

	private static final Logger LOGGER = LoggerFactory.getLogger(GroupResponseConsumer.class);

	@KafkaListener(topics = "topicOutbound", groupId = "producerGroup")
	public void consumeResponse(@Payload final ResponseMessage message) {

		LOGGER.info("Received Message: {}", message);

		if (appProps.getInstanceId().equals(message.getOrigin())) {

			LOGGER.info("Local message...");
			appContext.publishEvent(new MessageEvent(message));

		} else {
			if (StringUtils.isNotBlank(message.getOrigin()) && urlValidator.isValid(message.getCallback())) {

				final CrossResponse xResp = CrossResponse.builder().sender(appProps.getInstanceId()).response(message)
						.build();

				LOGGER.info("Redirecting to correct owner: {}", message.getOrigin());
				final ResponseEntity<String> responseEntity = restTemplate.postForEntity(message.getCallback(), xResp,
						String.class);
				LOGGER.info("Response from owner: {}", responseEntity.getStatusCode());

			} else {
				LOGGER.error("Invalid message content, not able to handle or redirect");
			}
		}

	}
}
