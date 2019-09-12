package com.github.ricardocomar.kafkabalancedconsumers.model;

import java.util.Optional;

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
	
	private Integer durationMin;
	private Integer durationMax;
	private Double processingRate;
	private Double callbackRate;
}
