package xivvic.event;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import xivvic.event.EventMetadata;
import xivvic.event.EventMetadataBase;

public class EventMetadataBaseTypeTest
{

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
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test(expected=IllegalStateException.class)
	public void testBuildNoType()
	{
		// Arrange
		//
		String ID = "ABC.123";
		
		// Act
		//
		@SuppressWarnings("unused")
		EventMetadata em = EventMetadataBase.builder()
				.id(ID)
				.build();
		
		
		// Assert
		//
		fail("Execution should have caused an exception before reaching this part of the test.");
	}

	@Test(expected=IllegalStateException.class)
	public void testBuildNullType()
	{
		// Arrange
		//
		String   ID = "ABC.123";
		EventType et = null;
		
		// Act
		//
		@SuppressWarnings("unused")
		EventMetadata em = EventMetadataBase.builder()
				.id(ID)
				.type(et)
				.build();
		
		
		// Assert
		//
		fail("Execution should have caused an exception before reaching this part of the test.");
	}

	@Test
	public void testBuildValidType()
	{
		// Arrange
		//
		String   ID = "ABC.123";
		EventType et = ExampleEventType.FILE_ARRIVAL;
		
		// Act
		//
		EventMetadata em = EventMetadataBase.builder()
				.id(ID)
				.type(et)
				.build();
		
		EventType result = em.type();
		
		
		// Assert
		//
		assertNotNull(em);
		assertTrue(et == result);
	}
}
