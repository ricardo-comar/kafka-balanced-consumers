package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

import org.apache.kafka.common.errors.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Service
public class ConcurrentProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentProcessor.class);
	
	@Autowired
	private KafkaTemplate<String, RequestMessage> template;
	
	@Value("${spring.kafka.producer.topicName}")
	String topicName;
	
	@Value("${kafkaConsummer.requestProcessor.waitTimeout}")
	Long waitTimeout;
	
	private final Map<String, RequestMessage> lockMap = new ConcurrentHashMap<String, RequestMessage>();
	private final Map<String, ResponseMessage> responseMap = new ConcurrentHashMap<String, ResponseMessage>();

	public ResponseMessage handle(RequestMessage request) throws TimeoutException {
		
		LOGGER.info("Message to be processed: {}", request);
		
		lockMap.remove(request.getId());
		responseMap.remove(request.getId());
		
		template.send(topicName, request);
		
		LOGGER.info("Will wait {}ms", waitTimeout);
		lockMap.put(request.getId(), request);
		synchronized (request) {
			try {
				request.wait(waitTimeout);
				LOGGER.info("Lock released for message {}", request.getId());
			} catch (InterruptedException e) {
				LOGGER.info("Wait timeout for message {}", request.getId());
				throw new TimeoutException("No response in " + waitTimeout + "ms");
			}
		}
		
		ResponseMessage response = responseMap.remove(request.getId());
		LOGGER.info("Returning response for id ({}) = {}", request.getId(), response);
		return response;
	}
	
	public boolean notifyResponse(ResponseMessage response) {
		if (!lockMap.containsKey(response.getId())) {
			LOGGER.info("Locked request not found for response id {}", response.getId());
			return false;
		}
		LOGGER.info("Response is being saved for id {}, lock will be released", response.getId());
		responseMap.put(response.getId(), response);
		RequestMessage request = lockMap.remove(response.getId());
		synchronized (request) {
			request.notify();
		}
		return true;
	}

}
