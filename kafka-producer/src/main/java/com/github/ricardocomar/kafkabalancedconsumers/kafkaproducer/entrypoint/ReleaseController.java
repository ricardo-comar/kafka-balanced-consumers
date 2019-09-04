package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model.CrossResponse;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.ConcurrentProcessorTest;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@RestController
public class ReleaseController {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ReleaseController.class);

	@Autowired
	private ConcurrentProcessorTest processor;

	@PostMapping(value = "/release")
	public ResponseEntity<?> process(
			@RequestBody CrossResponse request) {
		
		LOGGER.info("Response from outside: {}", request.getSender());

		Boolean success = processor.notifyResponse(ResponseMessage.builder()
				.id(request.getId())
				.duration(request.getDuration())
				.responseId(request.getResponseId())
				.duration(request.getDuration()).build());
		
		return (success ? ResponseEntity.ok() : ResponseEntity.notFound()).build() ;
	}
}
