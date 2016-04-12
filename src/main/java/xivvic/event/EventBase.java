package xivvic.event;

import java.util.Map;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EventBase
	implements Event
{
	abstract public EventMetadata metadata();

	@Nullable
	abstract public Object payload();

	@Nullable
	abstract public Map<String, Object> userHeaders();
	
	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder metadata(EventMetadata metadata);
		public abstract Builder userHeaders(Map<String, Object> map);
		public abstract Builder payload(Object payload);

		public abstract EventBase  build();
	}

	public static Builder builder()
	{
		return new AutoValue_EventBase.Builder();
	}


}
