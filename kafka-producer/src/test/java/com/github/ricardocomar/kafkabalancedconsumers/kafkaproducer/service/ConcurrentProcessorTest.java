package com.github.ricardocomar.kafkabalancedconsumers.kafkaproducer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;

import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {
		ConcurrentProcessor.class }, initializers = ConfigFileApplicationContextInitializer.class)
public class ConcurrentProcessorTest {

	@MockBean
	private KafkaTemplate<String, RequestMessage> template;

	@Autowired
	private ConcurrentProcessor processor;
	private Long duration;

	private Answer<?> templateAnswer = new Answer<ListenableFuture<SendResult<String, RequestMessage>>>() {


		@Override
		public ListenableFuture<SendResult<String, RequestMessage>> answer(InvocationOnMock invocation)
				throws Throwable {
			try {
				Thread.sleep(duration);
			} catch (Exception e) {
			}
			return null;
		}
	};

	@Before
	public void before() {
		duration = 100L;
		Mockito.when(template.send(Mockito.anyString(), Mockito.any(RequestMessage.class))).thenAnswer(templateAnswer);
	}

	@Test
	public void testSuccess() {
		duration = 1000L;
		processor.handle(RequestMessage.builder().build());
	}

}
