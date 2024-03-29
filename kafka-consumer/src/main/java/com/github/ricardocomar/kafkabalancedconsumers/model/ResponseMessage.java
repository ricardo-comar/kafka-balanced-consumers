package com.github.ricardocomar.kafkabalancedconsumers.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessage {
	private String id;
	private String origin;
	private String callback;
	private String responseId;
	private Integer duration;
}
