package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.config.AppProperties;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.exception.UnavailableResponseException;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service.model.MessageEvent;
import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Service
public class ConcurrentProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentProcessor.class);
	
	@Autowired
	private KafkaTemplate<String, RequestMessage> template;
	
	@Value("${spring.kafka.producer.topicName}")
	String topicName;
	
	@Autowired
	private AppProperties appProps;
	
	private final Map<String, RequestMessage> lockMap = new ConcurrentHashMap<String, RequestMessage>();
	private final Map<String, ResponseMessage> responseMap = new ConcurrentHashMap<String, ResponseMessage>();

	public ResponseMessage handle(RequestMessage request) throws UnavailableResponseException {
		
		LOGGER.info("Message to be processed: {}", request);
		
		lockMap.remove(request.getId());
		responseMap.remove(request.getId());
		
		template.send(topicName, request);
		
		LOGGER.info("Will wait {}ms", appProps.getConcurrentProcessor().getWaitTimeout());
		
		synchronized (lockMap) {
			lockMap.put(request.getId(), request);
		}

		synchronized (request) {
			try {
				request.wait(appProps.getConcurrentProcessor().getWaitTimeout());
				LOGGER.info("Lock released for message {}", request.getId());
			} catch (InterruptedException e) {
				LOGGER.info("Wait timeout for message {}", request.getId());
			} finally {
				synchronized (lockMap) {
					lockMap.remove(request.getId());
				}
			}
		}
		
		synchronized (lockMap) {
			ResponseMessage response = responseMap.remove(request.getId());
			if (response == null) {
				throw new UnavailableResponseException("No response for id " + request.getId());
			}

			LOGGER.info("Returning response for id ({}) = {}", request.getId(), response);
			return response;
		}
		
	}
	
	@EventListener
	public void notifyResponse(MessageEvent event) {
		
		ResponseMessage response = event.getResponse();
		if (!lockMap.containsKey(response.getId())) {
			LOGGER.warn("Locked request not found for response id {}", response.getId());
			return;
		}
		
		synchronized (lockMap) {
			RequestMessage request = lockMap.remove(response.getId());
			synchronized (request) {
				LOGGER.info("Response is being saved for id {}, lock will be released", response.getId());
				responseMap.put(response.getId(), response);

				request.notify();
			}
		}

	}

}
