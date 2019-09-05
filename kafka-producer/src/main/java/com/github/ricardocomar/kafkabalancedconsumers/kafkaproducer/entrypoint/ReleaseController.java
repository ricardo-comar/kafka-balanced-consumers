package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model.CrossResponse;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.ConcurrentProcessor;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@RestController
public class ReleaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseController.class);

	@Autowired
	private ConcurrentProcessor processor;

	@PostMapping(value = "/release")
	public ResponseEntity<?> process(@RequestBody CrossResponse request) {

		LOGGER.info("Response from outsider producer: {}", request.getSender());

		Long delay = new Random().ints(1, 100, 500).iterator().next().longValue();
		LOGGER.info("Randomic delay of {}ms", delay);
		try {
			TimeUnit.MILLISECONDS.sleep(delay);
		} catch (InterruptedException e) {
		}

		Boolean success = processor.notifyResponse(request.getResponse());

		return (success ? ResponseEntity.ok() : ResponseEntity.notFound()).build();
	}
}
