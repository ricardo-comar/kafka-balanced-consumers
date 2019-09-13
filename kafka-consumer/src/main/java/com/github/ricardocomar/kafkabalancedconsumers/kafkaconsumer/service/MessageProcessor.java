package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.repository.ProcessingEventRepository;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.repository.entity.EventState;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.repository.entity.ProcessingEvent;
import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Service
public class MessageProcessor {

	private static final Random RANDOM = new Random();
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);
	
	@Autowired
	private ProcessingEventRepository repo;

	public ResponseMessage process(RequestMessage request) {

		Integer durationMin = Optional.ofNullable(request.getDurationMin()).orElse(100);
		Integer durationMax = Optional.ofNullable(request.getDurationMax()).orElse(500);
		Double processingRate = Optional.ofNullable(request.getProcessingRate()).orElse(1.0);

		Integer sleep = RANDOM.ints(1, durationMin, durationMax).iterator().next();
		
		ProcessingEvent event = repo.findByRequestId(request.getId());
		if (event == null) {
			event = ProcessingEvent.builder().requestId(request.getId()).build();
		}
		event.setOrigin(request.getOrigin());
		event.setCallback(request.getCallback());
		event.setState(EventState.WIP);
		event.setStart(LocalDateTime.now());
		event.setDuration(sleep);
		event = save(event);

		LOGGER.info("Sleep time: {}", sleep);

		try {
			TimeUnit.MILLISECONDS.sleep(sleep);
		} catch (InterruptedException e) {
		}

		String responseId = (RANDOM.nextDouble() <= processingRate) ? UUID.randomUUID().toString() : "";
		
		event.setEnd(LocalDateTime.now());
		event.setResponseId(responseId);
		event.setState(StringUtils.isEmpty(responseId) ? EventState.ERROR : EventState.DONE);
		event = save(event);

		ResponseMessage response = ResponseMessage.builder().id(request.getId()).origin(request.getOrigin())
				.callback(request.getCallback()).responseId(responseId).duration(sleep).build();
		LOGGER.info("Returning response: {}", response);
		return response;
	}

	@Transactional(value = TxType.SUPPORTS)
	public ProcessingEvent save(ProcessingEvent event) {
		return repo.save(event);
	}
}
