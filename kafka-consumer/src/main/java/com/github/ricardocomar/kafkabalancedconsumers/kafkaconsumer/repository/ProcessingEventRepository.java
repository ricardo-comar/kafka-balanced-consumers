package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.repository;

import org.springframework.data.repository.CrudRepository;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.repository.entity.ProcessingEvent;

public interface ProcessingEventRepository extends CrudRepository<ProcessingEvent, Long> {
	
	ProcessingEvent findByRequestId(String requestId);

}
