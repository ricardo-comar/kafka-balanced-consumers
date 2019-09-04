package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Service
public class ConcurrentProcessor {
	
	@Autowired
	private KafkaTemplate<String, RequestMessage> template;
	
	@Value("${spring.kafka.producer.topicName}")
	private String topicName;
	
	@Value("${kafkaConsummer.requestProcessor.waitTimeout}")
	private Long waitTimeout;
	
	private final StampedLock lockImpl = new StampedLock();  
	
	private final Map<String, Long> lockMap = new ConcurrentHashMap<String, Long>();

	public ResponseMessage handle(RequestMessage request) throws TimeoutException {
		
//		Long lockKey = lockImpl.writeLock();
//		lockMap.put(request.getId(), lockKey);
//		
//		try {
//			lockImpl.tryWriteLock(waitTimeout, TimeUnit.MILLISECONDS);
//		} catch (InterruptedException e) {
//			throw new TimeoutException("No response in " + waitTimeout + "ms");
//		} finally {
//			lockImpl.unlockRead(lockMap.remove(request.getId()));
//		}
		
		template.send(topicName, request);
		
		return ResponseMessage.builder().id(request.getId()).build();
	}
	
	public boolean notifyResponse(ResponseMessage response) {
		return false;
	}

}
