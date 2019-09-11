package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.entrypoint.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessRequest {

	private String id;
	private Integer durationMin;
	private Integer durationMax;

	@Builder.Default
	private Double processingRate = 1.0;

	@Builder.Default
	private Double callbackRate = 1.0;

}
