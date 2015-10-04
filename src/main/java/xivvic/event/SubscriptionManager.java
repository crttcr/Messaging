package xivvic.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SubscriptionManager
{
	private final static Logger LOG = Logger.getLogger(SubscriptionManager.class.getName());

	
	private final Map<String, Subscription>                 subscriptions = new HashMap<>();
	
	public SubscriptionManager()
	{
	}
	
	public String subscribe(String pattern, EventPredicate predicate, EventCallback callback)
	{
		Subscription subscription = Subscription.create(pattern, predicate, callback);
		String                 id = subscription.id();
		
		String                msg = "Adding subscription with ID=" + id;
		LOG.info(msg);
		
		// TODO:  Subscriptions are handled in a primitive fashion.  It works for now.
		// However, at some point, we will probably want the broker to take responsibility
		// for deciding which messages go to which subscribers.  Iterating through the entire
		// list of subscribers for every message, isn't the greatest approach, but it is 
		// good enough (until it isn't).
		//
		subscriptions.put(id, subscription);
		
		return id;
	}
	
	// This method determines which subscriptions should be notified
	// of the event for this publication record
	//
	public List<Subscription> matchingSubscriptions(PublicationRecord record)
	{
		String           topic = record.topic();
		Event            event = record.event();
		
		List<Subscription> result = subscriptions.values()
				.stream()
				.filter(s -> s.topicMatches(topic))
				.filter(s -> s.predicate().test(event))
				.collect(Collectors.toList());

		return result;
	}

}
