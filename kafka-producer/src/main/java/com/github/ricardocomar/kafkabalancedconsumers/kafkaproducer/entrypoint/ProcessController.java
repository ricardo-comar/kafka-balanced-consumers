package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint;

import javax.annotation.PostConstruct;

import org.apache.kafka.common.errors.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model.ProcessRequest;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model.ProcessResponse;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.ConcurrentProcessorTest;
import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@RestController
public class ProcessController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessController.class);

	@Autowired
	private ConcurrentProcessorTest processor;

	@Value("${kafkaConsummer.instance_id}")
	private String instanceId;
	
	@Autowired 
	private Environment env;

	@PostMapping(value = "/process")
	public ResponseEntity<ProcessResponse> process(@RequestBody ProcessRequest request) {

		String callbackUrl = "http://localhost:" + env.getProperty("local.server.port") + "/release";

		try {
			ResponseMessage response = processor.handle(
					RequestMessage.builder()
					.id(request.getId())
					.origin(instanceId)
					.callback(callbackUrl)
					.durationMin(request.getDurationMin())
					.durationMax(request.getDurationMax())
					.build());

			return ResponseEntity.ok(ProcessResponse.builder().id(response.getId()).responseId(response.getResponseId())
					.duration(response.getDuration()).build());

		} catch (TimeoutException e) {
			LOGGER.error("Timeout handling request", e);
		}

		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
	}
}
