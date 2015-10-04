package xivvic.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import xivvic.event.Event;
import xivvic.event.EventBroker;
import xivvic.event.EventCallback;
import xivvic.event.EventMetadata;
import xivvic.event.SubscriptionManager;

public class EventBrokerTest
{
	private EventBroker     broker;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception
	{
		SubscriptionManager sm = new SubscriptionManager();
		broker = new EventBroker(sm);
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testSubmitMessageNullHeadersNoCorrelation()
	{
		String           topic = "test.message";
		String         payload = "event.payload";
		EventMetadata metadata = broker.submitMessage(topic, payload);
		String              id = metadata.id();
		
		assertNotNull(metadata);
		assertNotNull(id);
	}

	@Test
	public void testSubmitMessageEmptyHeaders()
	{
		String                topic = "test.message";
		String              payload = "event.payload";
		Map<String, Object> headers = new HashMap<>();
		EventMetadata      metadata = broker.submitMessage(topic, headers, payload);
		String                   id = metadata.id();
		
		assertNotNull(metadata);
		assertNotNull(id);
	}
	@Test
	public void testSubmitMessagePopulatedHeaders()
	{
		String                topic = "test.message";
		String              payload = "event.payload";
		String                  cid = "TEST_CORRELATION_ID";
		Map<String, Object> headers = new HashMap<>();
		
		headers.put("User.key.1", "User.value.1");
		headers.put("User.key.2", Boolean.TRUE);
		headers.put("User.key.3", LocalTime.now());
		
		EventMetadata      metadata = broker.submitMessage(topic, headers, cid, payload);
		String          id = metadata.id();
		
		assertNotNull(metadata);
		assertNotNull(id);
	}

	@Test
	public void testStartPublication()
	{
		// Arrange
		//
		
		// Act
		//
		boolean    success = broker.startPublishingEvents(0L, 200L);

		// Assert
		//
		assertTrue(success);
	}

	@Test
	public void testStopPublicationBeforeStarting()
	{
		// Arrange
		//
		
		// Act
		//
		boolean    success = broker.stopPublishingEvents();

		// Assert
		//
		assertFalse(success);
	}

	@Test
	public void testStartStopPublication()
	{
		// Arrange
		//
		
		// Act
		//
		boolean    start_success = broker.startPublishingEvents(0L, 200L);
		boolean     stop_success = broker.stopPublishingEvents();

		// Assert
		//
		assertTrue(start_success);
		assertTrue( stop_success);
	}

	@Test
	public void testStopPublicationViaMessage() throws Exception
	{
		// Arrange
		//
		
		// Act
		//
		boolean       start_success = broker.startPublishingEvents(0L, 200L);
		String                topic = EventBroker.TOPIC_COMMAND_SHUTDOWN;
		String              payload = "event.payload";
		EventMetadata      metadata = broker.submitMessage(topic, payload);
		String                   id = metadata.id();
		
		Thread.sleep(225);
		boolean        stop_success = broker.stopPublishingEvents();
		// Assert
		//
		assertNotNull(id);

		// Assert
		//
		assertTrue(start_success);
		assertFalse(stop_success);  // Should have already been shutdown by message.
	}
	
	
	@Test
	public void testSubscribe() throws Exception
	{
		// Arrange
		//
		String                 topic = "test.message";
		String               payload = "event.payload";
		EventCallback       callback = Mockito.mock(EventCallback.class);
		ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
		
		// Act
		//
		boolean        start_success = broker.startPublishingEvents(0L, 200L);
		String                   sid = broker.subscribe(topic, null, callback);
		EventMetadata       metadata = broker.submitMessage(topic, payload);
		String                    id = metadata.id();
		
		Thread.sleep(225);
		boolean        stop_success = broker.stopPublishingEvents();

		// Assert
		//
		assertTrue(start_success);
		assertTrue( stop_success);
		assertNotNull(id);
		assertNotNull(sid);
		Mockito.verify(callback, Mockito.times(1)).onMessage(captor.capture());
		Event event = captor.getValue();
		assertTrue(id.equals(event.metadata().id()));

	}

}
