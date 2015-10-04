package xivvic.event;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Subscription
{
	private Pattern topicRegex;
	
	/**
	 * This method determines whether or not a subscription matches
	 * a specific topic
	 * 
	 * TODO:  At some point, this should belong to the Broker, not the subscription
	 * 
	 * @param topic the candidate topic which this subscription might match.
	 * @return
	 */
	public final boolean topicMatches(String topic)
	{
		if (topicRegex != null)
		{
			Matcher m = topicRegex.matcher(topic);
			return  m.matches();
		}
		
		String topicPattern = topicPattern();
		return topicPattern.equalsIgnoreCase(topic);
	}
	
	public abstract String id();

	public abstract String topicPattern();
	
	@Nullable
	public abstract EventPredicate predicate();
	
	public abstract EventCallback callback();
	
	public static Subscription create(String pattern, EventPredicate predicate, EventCallback callback)
	{
		String id = UUID.randomUUID().toString();
		
		if (predicate == null)
			predicate = EventPredicate.TRUE;
		
		// Compile topicPattern into a regex
		//
		
		Subscription sub = new AutoValue_Subscription(id, pattern, predicate, callback);
		
		sub.topicRegex = Pattern.compile(pattern);

		return sub;
	}

}
