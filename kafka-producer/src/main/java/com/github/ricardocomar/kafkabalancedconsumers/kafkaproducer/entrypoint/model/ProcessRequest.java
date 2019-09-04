package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessRequest {

	private String id;
	private Integer durationMin;
	private Integer durationMax;
}
