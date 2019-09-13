package com.github.ricardocomar.kafkabalancedconsumers.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestMessage {
	private String id;
	private String origin;
	private String callback;

	@Builder.Default
	private Integer durationMin = 100;
	
	@Builder.Default
	private Integer durationMax = 500;

	@Builder.Default
	private Double processingRate = 1.0;

	@Builder.Default
	private Double callbackRate = 1.0;
}
