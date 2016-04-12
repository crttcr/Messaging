package xivvic.event;

import java.time.LocalTime;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EventMetadataBase
	implements EventMetadata
{

	public abstract String id();

	public abstract EventType type();

	
	@Nullable
	public abstract String correlationId();

	public abstract LocalTime publishedAt();
	
	
	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder id(String id);
		public abstract Builder type(EventType type);
		public abstract Builder correlationId(String cid);
		public abstract Builder publishedAt(LocalTime time);

		public abstract EventMetadataBase  build();
	}

	public static Builder builder()
	{
		LocalTime now = LocalTime.now();
		
		return new AutoValue_EventMetadataBase.Builder()
			.publishedAt(now);
	}


}
