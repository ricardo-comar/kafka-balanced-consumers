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

	private static final Random RANDOM = new Random();
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

	public ResponseMessage process(RequestMessage request) {

		Optional<Integer> minOpt = Optional.ofNullable(request.getDurationMin());
		Optional<Integer> maxOpt = Optional.ofNullable(request.getDurationMax());

		Long sleep = RANDOM.ints(1, minOpt.orElse(100), maxOpt.orElse(500)).iterator().next().longValue();

		LOGGER.info("Sleep time: {}", sleep);

		try {
			TimeUnit.MILLISECONDS.sleep(sleep);
		} catch (InterruptedException e) {
		}

		String string = (RANDOM.nextDouble() <= Optional.ofNullable(request.getProcessingRate()).orElse(1.0)) ? UUID.randomUUID().toString() : "";

		ResponseMessage response = ResponseMessage.builder().id(request.getId()).origin(request.getOrigin())
				.callback(request.getCallback()).responseId(string).duration(sleep)
				.callbackRate(request.getCallbackRate()).processingRate(request.getProcessingRate()).build();
		LOGGER.info("Returning response: {}", response);
		return response;
	}

}
