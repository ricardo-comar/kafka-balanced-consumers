package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.consumer;

import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

public interface ResponseConsumer {
	
	public void consumeResponse(ResponseMessage message) ;
}
