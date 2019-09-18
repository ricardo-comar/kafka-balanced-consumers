package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.config.AppProperties;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.exception.UnavailableResponseException;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.model.MessageEvent;
import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Service
public class ConcurrentProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentProcessor.class);

	private static final String TOPIC_PRODUCER = "topicInbound";

	@Autowired
	private KafkaTemplate<String, RequestMessage> template;

	@Autowired
	private AppProperties appProps;

	private final Map<String, RequestMessage> lockMap = new ConcurrentHashMap<>();
	private final Map<String, ResponseMessage> responseMap = new ConcurrentHashMap<>();

	public ResponseMessage handle(final RequestMessage request)
			throws UnavailableResponseException {

		LOGGER.info("Message to be processed: {}", request);

		lockMap.remove(request.getId());
		responseMap.remove(request.getId());

		final Message<RequestMessage> msg = MessageBuilder.withPayload(request)
				.setHeader(KafkaHeaders.CORRELATION_ID, MDC.get(AppProperties.PROP_CORRELATION_ID))
				.setHeader(KafkaHeaders.TOPIC, TOPIC_PRODUCER).build();

		template.send(msg);

		LOGGER.info("Will wait {}ms", appProps.getConcurrentProcessor().getWaitTimeout());

		synchronized (lockMap) {
			lockMap.put(request.getId(), request);
		}

		synchronized (request) {
			try {
				request.wait(appProps.getConcurrentProcessor().getWaitTimeout());
				LOGGER.info("Lock released for message {}", request.getId());
			} catch (final InterruptedException e) {
				LOGGER.info("Wait timeout for message {}", request.getId());
			} finally {
				synchronized (lockMap) {
					lockMap.remove(request.getId());
				}
			}
		}

		synchronized (lockMap) {
			final ResponseMessage response = responseMap.remove(request.getId());
			if (response == null) {
				throw new UnavailableResponseException("No response for id " + request.getId());
			}

			LOGGER.info("Returning response for id ({}) = {}", request.getId(), response);
			return response;
		}

	}

	@EventListener
	public void notifyResponse(final MessageEvent event) {

		final ResponseMessage response = event.getResponse();
		if (!lockMap.containsKey(response.getId())) {
			LOGGER.warn("Locked request not found for response id {}", response.getId());
			return;
		}

		synchronized (lockMap) {
			final RequestMessage request = lockMap.remove(response.getId());
			synchronized (request) {
				LOGGER.info("Response is being saved for id {}, lock will be released", response.getId());
				responseMap.put(response.getId(), response);

				request.notify();
			}
		}

	}

	public Double getCallbackRate(final String id) {
		return Optional
				.ofNullable(
						Optional.ofNullable(lockMap.get(id)).orElse(RequestMessage.builder().build()).getCallbackRate())
				.orElse(1.0);
	}

}
