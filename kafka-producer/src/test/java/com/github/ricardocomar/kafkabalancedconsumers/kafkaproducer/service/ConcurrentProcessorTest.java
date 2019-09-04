package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = { ConcurrentProcessor.class }
, initializers = ConfigFileApplicationContextInitializer.class)
public class ConcurrentProcessorTest {

	@MockBean
	private KafkaTemplate<String, RequestMessage> template;

	@Autowired
	private ConcurrentProcessor processor;
	
	@Before
	public void before() {
		Mockito.when(template.send(Mockito.anyString(), Mockito.any(RequestMessage.class))).thenReturn(null);
	}

	@Test
	public void testSuccess() {
		
		processor.handle(RequestMessage.builder().build());
	}

}
