package com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.sql.DataSource;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;

import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.config.AppProperties;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.config.DatabaseConfiguration;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.producer.ReturnProducer;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.repository.ProcessingEventRepository;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.repository.entity.EventState;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.repository.entity.ProcessingEvent;
import com.github.ricardocomar.kafkabalancedconsumers.kafkaconsumer.service.MessageProcessor;
import com.github.ricardocomar.kafkabalancedconsumers.model.RequestMessage;
import com.github.ricardocomar.kafkabalancedconsumers.model.ResponseMessage;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(AppProperties.class)
@ContextConfiguration(classes = { DatabaseConfiguration.class, MessageConsumer.class, MessageProcessor.class,
		ReturnProducer.class }, initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@DataJpaTest
public class MessageConsumerIntegrationTest {

	@MockBean
	private KafkaTemplate<String, ResponseMessage> kafkaTemplate;

	@Autowired
	private MessageConsumer consumer;

	@Autowired
	private ProcessingEventRepository repo;

	@Autowired
	private DataSource ds;

	private ResponseMessage response;
	final String requestId = "AAA";
	final String origin = "testClass";
	final String callback = "http://localhost:1234/callbackMock";

	@Before
	public void before() throws Exception {
		response = null;
		Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any(ResponseMessage.class)))
				.thenAnswer(new Answer<ListenableFuture<SendResult<String, ResponseMessage>>>() {

					@Override
					public ListenableFuture<SendResult<String, ResponseMessage>> answer(InvocationOnMock invocation)
							throws Throwable {
						response = invocation.getArgument(1);
						return null;
					}
				});
		ds.getConnection().createStatement().executeUpdate("delete from processing_event");
		ds.getConnection().commit();
	}

	@Test
	public void testNewProcessing() {

		assertThat(countRecords(), equalTo(0));
		ProcessingEvent processingEvent = repo.findByRequestId(requestId);
		assertThat(processingEvent, nullValue());

		RequestMessage request = RequestMessage.builder().id(requestId).origin(origin).durationMin(100).durationMax(200)
				.callback(callback).build();
		consumer.listenToParition(request, 0);

		processingEvent = repo.findByRequestId(requestId);
		reviewEventAndResponse(request, processingEvent, EventState.DONE, not(emptyString()));
	}

	@Test
	public void testErrorProcessing() {

		assertThat(countRecords(), equalTo(0));
		ProcessingEvent processingEvent = repo.findByRequestId(requestId);
		assertThat(processingEvent, nullValue());

		RequestMessage request = RequestMessage.builder().id(requestId).origin(origin).durationMin(100).durationMax(200)
				.callback(callback).processingRate(0.0).build();
		consumer.listenToParition(request, 0);

		processingEvent = repo.findByRequestId(requestId);
		reviewEventAndResponse(request, processingEvent, EventState.ERROR, emptyString());
	}

	@Test
	public void testReProcessing() {

		assertThat(countRecords(), equalTo(0));
		ProcessingEvent prevEvent = ProcessingEvent.builder().state(EventState.ERROR).requestId(requestId)
				.origin(origin).callback(callback).start(LocalDateTime.now().minusMinutes(11))
				.end(LocalDateTime.now().minusMinutes(10)).responseId("AAA").duration(100).build();
		repo.save(prevEvent);

		ProcessingEvent processingEvent = repo.findByRequestId(requestId);
		assertThat(processingEvent, equalTo(prevEvent));

		RequestMessage request = RequestMessage.builder().id(requestId).origin(origin).durationMin(100).durationMax(200)
				.callback(callback).build();
		consumer.listenToParition(request, 0);

		processingEvent = repo.findByRequestId(requestId);
		reviewEventAndResponse(request, processingEvent, EventState.DONE, not(emptyString()));
	}

	@Test
	public void testWipProcessing() {

		assertThat(countRecords(), equalTo(0));
		ProcessingEvent processingEvent = repo.findByRequestId(requestId);
		assertThat(processingEvent, nullValue());

		RequestMessage request = RequestMessage.builder().id(requestId).origin(origin).durationMin(1000)
				.durationMax(1500).callback(callback).build();

		new Thread(new Runnable() {
			public void run() {
				consumer.listenToParition(request, 0);
			}
		}).start();
		sleep(50);

		assertThat(countRecords(), equalTo(1));

		processingEvent = repo.findByRequestId(requestId);
		assertThat(response, nullValue());
		assertThat(processingEvent.getRequestId(), equalTo(request.getId()));
		assertThat(processingEvent.getOrigin(), equalTo(request.getOrigin()));
		assertThat(processingEvent.getState(), equalTo(EventState.WIP));
		assertThat(processingEvent.getCallback(), equalTo(request.getCallback()));
		assertThat(processingEvent.getStart(), lessThanOrEqualTo(LocalDateTime.now()));
		assertThat(processingEvent.getDuration(), notNullValue());
	}

	private int countRecords() {
		try {
			ResultSet resultSet = ds.getConnection("sa", "").createStatement()
					.executeQuery("select count(*) from processing_event");
			resultSet.first();
			return resultSet.getInt(1);

		} catch (SQLException e) {
		}
		return -1;
	}

	private void reviewEventAndResponse(RequestMessage request, ProcessingEvent processingEvent, EventState state,
			Matcher<String> matcherResponseId) {
		assertThat(processingEvent, notNullValue());
		assertThat(processingEvent.getId(), notNullValue());
		assertThat(processingEvent.getState(), equalTo(state));
		assertThat(processingEvent.getRequestId(), equalTo(request.getId()));
		assertThat(processingEvent.getOrigin(), equalTo(request.getOrigin()));
		assertThat(processingEvent.getCallback(), equalTo(request.getCallback()));
		assertThat(processingEvent.getStart(), lessThanOrEqualTo(LocalDateTime.now()));
		assertThat(processingEvent.getEnd(), lessThanOrEqualTo(LocalDateTime.now()));
		assertThat(processingEvent.getEnd(), greaterThan(processingEvent.getStart()));
		assertThat(processingEvent.getResponseId(), matcherResponseId);
		assertThat(processingEvent.getDuration(), notNullValue());
		assertThat(processingEvent.getDuration(), is(
				both(greaterThanOrEqualTo(request.getDurationMin())).and(lessThanOrEqualTo(request.getDurationMax()))));
		assertThat(processingEvent.getEnd(),
				lessThanOrEqualTo(LocalDateTime.now().plus(processingEvent.getDuration(), ChronoUnit.MILLIS)));

		assertThat(response, notNullValue());
		assertThat(response.getId(), equalTo(request.getId()));
		assertThat(response.getOrigin(), equalTo(request.getOrigin()));
		assertThat(response.getResponseId(), equalTo(processingEvent.getResponseId()));
		assertThat(response.getCallback(), equalTo(processingEvent.getCallback()));
		assertThat(response.getDuration(), equalTo(processingEvent.getDuration()));
	}

	private void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
		}
	}
}
