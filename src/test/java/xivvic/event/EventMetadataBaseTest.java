package xivvic.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalTime;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import xivvic.event.EventMetadata;
import xivvic.event.EventMetadataBase;

public class EventMetadataBaseTest
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

	@Test
	public void testId()
	{
		String ID = "This will serve as an ID";
		
		EventMetadata em = EventMetadataBase.builder()
				.id(ID)
				.build();
		
		
		// Assert
		//
		assertNotNull(em);
		assertTrue(ID.equals(em.id()));
	}

	@Test
	public void testCorrelationId()
	{
		String  ID = "This will serve as an ID";
		String CID = "1234";
		
		EventMetadata em = EventMetadataBase.builder()
				.id(ID)
				.correlationId(CID)
				.build();
		
		
		// Assert
		//
		assertNotNull(em);
		assertTrue(CID.equals(em.correlationId()));
	}

	@Test
	public void testPublishedAtDefault()
	{
		String ID = "This will serve as an ID";
		
		EventMetadata em = EventMetadataBase.builder()
				.id(ID)
				.build();
		
		LocalTime eventTime = em.publishedAt();
		
		// Assert
		//
		assertNotNull(eventTime);
	}

	@Test
	public void testPublishedAtExplicit()
	{
		String     ID = "This will serve as an ID";
		LocalTime now = LocalTime.now();
		
		EventMetadata em = EventMetadataBase.builder()
				.id(ID)
				.publishedAt(now)
				.build();
		
		LocalTime eventTime = em.publishedAt();
		
		// Assert
		//
		assertEquals(now, eventTime);
	}

}
