package xivvic.event;

import java.util.Map;

public interface EventPublisher
{
	public EventMetadata publish(String topic, Map<String, Object> headers, String correlation, Object payload);
}
