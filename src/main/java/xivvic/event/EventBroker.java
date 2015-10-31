package xivvic.event;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBroker
{
	public static final String TOPIC_COMMAND_SHUTDOWN = "eventbroker.command.shutdown"; 
	public static final String TOPIC_COMMAND_PURGE    = "eventbroker.command.purge"; 

	private final static Logger LOG = LoggerFactory.getLogger(EventBroker.class.getName());

	
	private ScheduledExecutorService                             executor = null;
	private final ConcurrentLinkedDeque<PublicationRecord> readyToPublish = new ConcurrentLinkedDeque<>();
	private final Map<String, Runnable>                        commandMap = new HashMap<>();
	private final SubscriptionManager                             sub_man;
	
	public EventBroker(SubscriptionManager sub_man)
	{
		this.sub_man = sub_man;
		
		// Prepare a map for containing commands to execute when
		// broker command messages are received in the message stream
		//
		commandMap.put(TOPIC_COMMAND_SHUTDOWN, shutdownFunction());
		commandMap.put(TOPIC_COMMAND_PURGE   , purgeFunction());
	}
	
	public boolean startPublishingEvents(long initialDelayMillis, long delayMillis)
	{
		if (executor != null)
		{
			if (! executor.isShutdown())
			{
				String msg = "Request to start publishing events with functioning publication thread";
				LOG.warn(msg);
				return false;
			}
				
			if (! executor.isTerminated())
			{
				String msg = "Executor has been shutdown, but not all tasks have completed.";
				LOG.warn(msg);
				return false;
			}
		}

		executor = Executors.newScheduledThreadPool(1);

		// Arrange for a new thread to run in a separate thread
		//
		Runnable task = () ->
		{
			publicationLoop();
		};
		
		executor.scheduleWithFixedDelay(task, initialDelayMillis, delayMillis, TimeUnit.MILLISECONDS);
		
		return true;
	}

	public boolean stopPublishingEvents()
	{
		if (executor == null)
		{
			String msg = "Request to stop event publication when there is no active publication";
			LOG.warn(msg);
			return false;
		}
			
		if (executor.isTerminated())
		{
			String msg = "Request to stop event publication when termination already occurred";
			LOG.warn(msg);
			return false;
		}
				
		if (executor.isShutdown())
		{
			String msg = "Request to stop publishing when Executor has been shutdown, but not all tasks have completed.";
			LOG.warn(msg);
			return false;
		}
		
		executor.shutdown();
      try
		{
			executor.awaitTermination(1, TimeUnit.SECONDS);
		}
		catch (InterruptedException e)
		{
			String msg = "Termination interrupted: " + e;
			LOG.warn(msg);
		}

      executor = null;
		return true;
	}

	/** 
	 * Publish an event and receive a unique ID as a reference
	 * This is a convenience form of the full publication message that omits user 
	 * headers and the correlation id.
	 * 
	 * @param topic a string representing the domain of relevance for this message
	 * @param headers user-defined header attributes
	 * @param payload the content of the message's body
	 * 
	 * @return a unique ID for the message
	 */
	public EventMetadata submitMessage(String topic, Object payload)
	{
		return this.submitMessage(topic, null, null, payload);
	}

	/** 
	 * Publish an event and receive a unique ID as a reference
	 * This is a convenience form of the full publication message that omits the correlation id.
	 * 
	 * @param topic a string representing the domain of relevance for this message
	 * @param headers user-defined header attributes
	 * @param payload the content of the message's body
	 * 
	 * @return a unique ID for the message
	 */
	public EventMetadata submitMessage(String topic, Map<String, Object> headers, Object payload)
	{
		return this.submitMessage(topic, headers, null, payload);
	}

	/** 
	 * Publish an event and receive a unique ID as a reference
	 * 
	 * @param topic a string representing the domain of relevance for this message
	 * @param headers user-defined header attributes
	 * @param correlation an identifier that can be used to associate this message with a previous message
	 * @param payload the content of the message's body
	 * 
	 * @return a unique ID for the message
	 */
	public EventMetadata submitMessage(String topic, Map<String, Object> headers, String correlation, Object payload)
	{
		String    uid = UUID.randomUUID().toString();
		String    cid = correlation != null ? correlation : UUID.randomUUID().toString();
		LocalTime now = LocalTime.now();
		
		EventMetadata metadata = EventMetadataBase.builder()
				.id(uid)
				.correlationId(cid)
				.publishedAt(now)
				.build();
				
		Event ev = EventBase.builder()
				.metadata(metadata)
				.userHeaders(headers)
				.payload(payload)
				.build();
		
		System.out.println(ev);
		
		LOG.info("Prepare for publication: " + ev);
		
		prepareToPublish(topic, ev);
		return metadata;
	}
	
	public String subscribe(String pattern, EventPredicate predicate, EventCallback callback)
	{
		return sub_man.subscribe(pattern, predicate, callback);
	}
	
	private void prepareToPublish(String topic, Event e)
	{
		PublicationRecord record = PublicationRecord.create(topic, e);
		
		readyToPublish.addLast(record);
	}

	private void publicationLoop()
	{
		while (! readyToPublish.isEmpty())
		{
			PublicationRecord record = readyToPublish.removeFirst();
			String             topic = record.topic();
			Runnable         command = commandMap.get(topic);
			
			if (command != null)
			{
				command.run();

				if (executor.isShutdown())
					return;
			}
			else
			{
				publishOneEvent(record);
			}
		}
	}
	
	// Return a function that will
	// shut down publication when invoked.
	// 
	private Runnable shutdownFunction()
	{
		Runnable r = () ->
		{
			executor.shutdown();
			int count = readyToPublish.size();
			if (count > 0)
			{
				String msg = "Shutdown command receive in message queue.  Purging " + count + " messages.";
				LOG.warn(msg);
			}
			readyToPublish.clear();
		};
		
		return r;
	}

	// Return a function that will
	// drain the event queue
	// 
	private Runnable purgeFunction()
	{
		Runnable r = () ->
		{
			int  count = readyToPublish.size();
			String msg = "Purge command received. ";

			if (count == 1)
			{
				msg += "Deleting 1 message.";
			}
			else
			{
				msg += "Deleting " + count + " messages.";
			}

			LOG.info(msg);
			readyToPublish.clear();
		};
		
		return r;
	}

	// This method makes the actual callback, after retrieving
	// a list of subscriptions.
	//
	private void publishOneEvent(PublicationRecord record)
	{
		Event                 event = record.event();
		List<Subscription> toNotify = sub_man.matchingSubscriptions(record);
		
		for (Subscription sub : toNotify)
		{
			EventCallback cb = sub.callback();
			try
			{
				cb.onMessage(event);
			}
			catch (Exception e)
			{
				String msg = "Exception thrown by callback method: " + e.getLocalizedMessage();
				LOG.warn(msg);
			}
		}
	}
	
}
