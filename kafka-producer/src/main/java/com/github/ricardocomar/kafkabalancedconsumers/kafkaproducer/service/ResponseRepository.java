package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@Component
@CacheConfig(cacheNames={"MessageResponses"}) 
public class ResponseRepository {

	@CachePut(key = "#response.id")
	public ResponseMessage saveResponse(ResponseMessage response) {
		return response;
	}
	
	@Cacheable(key = "#id")
	public ResponseMessage retrieveResponse(String id){
		return null;
	}

}
