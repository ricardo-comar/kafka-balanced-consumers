package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestMessage {

	private String origin;
	
	private Integer durationMin;
	
	private Integer durationMax;
}
