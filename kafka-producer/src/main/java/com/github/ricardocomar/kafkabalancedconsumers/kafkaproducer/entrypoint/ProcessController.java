package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.config.AppProperties;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model.ProcessRequest;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model.ProcessResponse;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.exception.UnavailableResponseException;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.ConcurrentProcessor;
import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@RestController
public class ProcessController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessController.class);

	@Autowired
	private ConcurrentProcessor processor;

	@Autowired 
	private Environment env;
	
	@Autowired
	private AppProperties appProps;

	@PostMapping(value = "/process")
	public ResponseEntity<ProcessResponse> process(@RequestBody ProcessRequest request) {

		String callbackUrl = "http://localhost:" + env.getProperty("local.server.port") + "/release";

		try {
			ResponseMessage response = processor.handle(
					RequestMessage.builder()
					.id(UUID.randomUUID().toString())
					.origin(appProps.getInstanceId())
					.callback(callbackUrl)
					.durationMin(request.getDurationMin())
					.durationMax(request.getDurationMax())
					.build());

			return ResponseEntity.ok(ProcessResponse.builder().id(request.getId()).responseId(response.getResponseId())
					.duration(response.getDuration()).build());

		} catch (UnavailableResponseException e) {
			LOGGER.error("Response Unavailable");
		}

		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
	}
}
