package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	public ResponseEntity<ProcessResponse> process(@RequestBody final ProcessRequest request) {

		final String callbackUrl = "http://localhost:" + env.getProperty("local.server.port") + "/release";

		try {
			final RequestMessage requestMessage = RequestMessage.builder().id(UUID.randomUUID().toString())
					.origin(appProps.getInstanceId()).callback(callbackUrl)
					.processingRate(request.getProcessingRate())
					.callbackRate(request.getCallbackRate())
					.durationMin(request.getDurationMin())
					.durationMax(request.getDurationMax()).build();

			final ResponseMessage response = processor.handle(requestMessage);

			return (!StringUtils.isEmpty(response.getResponseId()) ?  ResponseEntity.ok()
					: ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR))
							.body(ProcessResponse.builder().id(request.getId()).responseId(response.getResponseId())
									.duration(response.getDuration()).build());

		} catch (final UnavailableResponseException e) {
			LOGGER.error("Response Unavailable");
		}

		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
	}
}
