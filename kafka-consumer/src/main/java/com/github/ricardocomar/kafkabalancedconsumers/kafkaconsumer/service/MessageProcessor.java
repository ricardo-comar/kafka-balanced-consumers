package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.service;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Service
public class MessageProcessor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MessageProcessor.class);

	public ResponseMessage process(RequestMessage request) {

		Optional<Integer> minOpt = Optional
				.ofNullable(request.getDurationMin());
		Optional<Integer> maxOpt = Optional
				.ofNullable(request.getDurationMax());

		Long sleep = new Random()
				.ints(1, minOpt.orElse(100), maxOpt.orElse(500)).iterator()
				.next().longValue();

		LOGGER.info("Sleep time: {}", sleep);
		ResponseMessage response = ResponseMessage.builder()
				.id(request.getId())
				.origin(request.getOrigin())
				.callback(request.getCallback())
				.responseId(UUID.randomUUID().toString())
				.duration(sleep)
				.build();

		try {
			TimeUnit.MILLISECONDS.sleep(sleep);
		} catch (InterruptedException e) {
		}

		System.out.println("Sleep time");
		return response;
	}

}
