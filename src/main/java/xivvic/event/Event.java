package xivvic.event;

import java.util.Map;

public interface Event
{
	EventMetadata metadata();
	
	Map<String, Object> userHeaders();

	Object payload();
}
