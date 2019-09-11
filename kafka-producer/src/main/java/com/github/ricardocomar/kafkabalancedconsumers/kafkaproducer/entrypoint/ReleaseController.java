package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model.CrossResponse;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.ConcurrentProcessor;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.model.MessageEvent;

@RestController
public class ReleaseController {

	private static final Random RANDOM = new Random();

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseController.class);

	@Autowired
	private ConcurrentProcessor processor;

	@Autowired
	private ApplicationContext appContext;

	@PostMapping(value = "/release")
	public ResponseEntity<?> process(@RequestBody CrossResponse request) {

		LOGGER.info("Response from outsider producer: {}", request.getSender());

		if (!(RANDOM.nextDouble() <= Optional.ofNullable(request.getResponse().getCallbackRate()).orElse(1.0))) {
			LOGGER.warn("Simulating response failure");
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		}

		Long delay = RANDOM.ints(1, 150, 250).iterator().next().longValue();
		LOGGER.info("Randomic delay of {}ms", delay);
		try {
			TimeUnit.MILLISECONDS.sleep(delay);
		} catch (InterruptedException e) {
		}

		appContext.publishEvent(new MessageEvent(request.getResponse()));

		return ResponseEntity.ok().build();
	}
}
