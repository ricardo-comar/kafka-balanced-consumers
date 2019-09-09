package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.exception.UnavailableResponseException;
import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

public interface MessageProcessor {

	ResponseMessage handle(RequestMessage request) throws UnavailableResponseException;

}