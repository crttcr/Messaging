package xivvic.event;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import xivvic.util.identity.RandomString;

/**
 * Simple test to make sure the specification of our AutoValue class is as expected.
 * This includes visibility of accessors and the builder.
 * 
 * This also serves as an example of how the class can be constructed.
 * 
 * @author reid.dev
 *
 */
public class EventBaseTest
{
	private static RandomString randomString = new RandomString(8);
	
	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	private EventMetadata exampleMetadata()
	{
		EventMetadataBase.Builder builder = EventMetadataBase.builder();
		
		builder.id(randomString.nextString());
		builder.type(ExampleEventType.PROCESSING_COMPLETED);
		
		EventMetadata metadata = builder.build();
		
		return metadata;
	}

	@Test
	public void testBuilder()
	{
		// Arrange
		//
		
		// Act
		//
		EventBase.Builder builder = EventBase.builder();
		
		// Assert
		//
		assertNotNull(builder);
	}

	@Test(expected=IllegalStateException.class)
	public void testNullMetadata()
	{
		// Arrange
		//
		EventMetadata        meta = null;
		Map<String, Object>   map = new HashMap<>();
		EventBase.Builder builder = EventBase.builder();
		
		builder.payload(new Object());
		builder.userHeaders(map);
		builder.metadata(meta);
		
		// Act
		//
		@SuppressWarnings("unused")
		Event e = builder.build();
		
		// Assert
		//
		fail("Execution should have caused an exception before reaching this part of the test.");
	}

	@Test
	public void testEmptyHeaders()
	{
		// Arrange
		//
		EventMetadata        meta = exampleMetadata();
		Map<String, Object>   map = new HashMap<>();
		EventBase.Builder builder = EventBase.builder();
		
		builder.payload(new Object());
		builder.userHeaders(map);
		builder.metadata(meta);

		Event e = builder.build();
		
		// Act
		//
		Map<String, Object> result = e.userHeaders();
		
		// Assert
		//
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	public void testNonEmptyUserHeaders()
	{
		// Arrange
		//
		String                 k1 = "key_string";
		String                 k2 = "key_integer";
		String                 v1 = "some string value";
		Integer                v2 = 34;
		EventMetadata        meta = exampleMetadata();
		Map<String, Object>   map = new HashMap<>();
		EventBase.Builder builder = EventBase.builder();
		
		map.put(k1, v1);
		map.put(k2, v2);
		builder.payload(new Object());
		builder.userHeaders(map);
		builder.metadata(meta);

		Event e = builder.build();
		
		// Act
		//
		Map<String, Object> result = e.userHeaders();
		String            value_a = (String)  result.get(k1);
		Integer           value_b = (Integer) result.get(k2);
		
		// Assert
		//
		assertNotNull(result);
		assertNotNull(value_a);
		assertNotNull(value_b);
		assertTrue(v1.equals(value_a));
		assertTrue(v2.equals(value_b));
	}

	@Test
	public void testNullPayload()
	{
		// Arrange
		//
		EventMetadata        meta = exampleMetadata();
		Map<String, Object>   map = new HashMap<>();
		EventBase.Builder builder = EventBase.builder();
		
		builder.userHeaders(map);
		builder.metadata(meta);
		builder.payload(null);
		
		// Act
		//
		Event       e = builder.build();
		Object result = e.payload();
		
		// Assert
		//
		assertNotNull(e);
		assertNull(result);
	}

	@Test
	public void testNonNullPayload()
	{
		// Arrange
		//
		EventMetadata        meta = exampleMetadata();
		Map<String, Object>   map = new HashMap<>();
		EventBase.Builder builder = EventBase.builder();
		Object            payload = new Object();
		
		builder.userHeaders(map);
		builder.metadata(meta);
		builder.payload(payload);
		
		// Act
		//
		Event       e = builder.build();
		Object result = e.payload();
		
		// Assert
		//
		assertNotNull(result);
		assertTrue(payload.equals(result));
	}

}
