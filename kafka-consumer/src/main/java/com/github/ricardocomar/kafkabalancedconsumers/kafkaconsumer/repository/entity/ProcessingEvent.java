package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.repository.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class ProcessingEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private EventState state;
	
	@Column(unique = true, nullable = false)
	private String requestId;
	
	@Column(nullable = false)
	private String origin;
	
	@Column(nullable = false)
	private String callback;
	
	@Column(nullable = false)
	private LocalDateTime start;

	private LocalDateTime end;
	
	private String responseId;

	private Integer duration;
}
