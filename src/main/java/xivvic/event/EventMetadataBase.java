package xivvic.event;

import java.time.LocalTime;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EventMetadataBase
	implements EventMetadata
{

	abstract public String id();

	
	@Nullable
	abstract public String correlationId();

	abstract public LocalTime publishedAt();
	
	
	@AutoValue.Builder
	abstract static class Builder
	{
		abstract EventMetadataBase  build();
		abstract Builder id(String id);
		abstract Builder correlationId(String cid);
		abstract Builder publishedAt(LocalTime time);
	}

	public static Builder builder()
	{
		LocalTime now = LocalTime.now();
		
		return new AutoValue_EventMetadataBase.Builder()
			.publishedAt(now);
	}


}
