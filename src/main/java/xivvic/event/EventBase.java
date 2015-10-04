package xivvic.event;

import java.util.Map;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EventBase
	implements Event
{

	abstract public EventMetadata metadata();

	abstract public Object payload();

	@Nullable
	abstract public Map<String, Object> userHeaders();
	
	@AutoValue.Builder
	abstract static class Builder
	{
		abstract EventBase  build();
		abstract Builder metadata(EventMetadata metadata);
		abstract Builder userHeaders(Map<String, Object> map);
		abstract Builder payload(Object payload);
	}

	public static Builder builder()
	{
		return new AutoValue_EventBase.Builder();
	}


}
