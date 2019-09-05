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
	
	private final StampedLock lockImpl = new StampedLock();  
	
	private final Map<String, Long> lockMap = new ConcurrentHashMap<String, Long>();
	private final Map<String, ResponseMessage> responseMap = new ConcurrentHashMap<String, ResponseMessage>();

	public ResponseMessage handle(RequestMessage request) throws TimeoutException {
		
		LOGGER.info("Message to be processed: {}", request);
		
		lockMap.remove(request.getId());
		responseMap.remove(request.getId());
		
		template.send(topicName, request);

		Long lockKey = lockImpl.writeLock();
		lockMap.put(request.getId(), lockKey);
		LOGGER.info("Lock({}) for message {}", lockKey, request.getId());
		
		try {
			LOGGER.info("Will wait {}ms", waitTimeout);
			long tryWriteLock = lockImpl.tryWriteLock(waitTimeout, TimeUnit.MILLISECONDS);
			LOGGER.info("Lock released for message {}", request.getId());
		} catch (InterruptedException e) {
			LOGGER.info("Wait timeout for message {}", request.getId());
			throw new TimeoutException("No response in " + waitTimeout + "ms");
		} finally {
			lockImpl.unlockWrite(lockMap.remove(request.getId()));
		}

		LOGGER.info("Returning response for message {}", request.getId());
		return responseMap.remove(request.getId());
	}
	
	public boolean notifyResponse(ResponseMessage response) {
		if (!lockMap.containsKey(response.getId())) {
			LOGGER.info("Lock id not found for response id {}", response.getId());
			return false;
		}
		LOGGER.info("Response is being saved for id {}, lock will be released", response.getId());
		responseMap.put(response.getId(), response);
		lockImpl.unlockWrite(lockMap.get(response.getId()));
		return true;
	}

}
