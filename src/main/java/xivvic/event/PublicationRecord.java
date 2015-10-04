package xivvic.event;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PublicationRecord
{
	public abstract String topic();
	
	public abstract Event event();
	
	public static PublicationRecord create(String topic, Event event)
	{
		return new AutoValue_PublicationRecord(topic, event);
	}

}
