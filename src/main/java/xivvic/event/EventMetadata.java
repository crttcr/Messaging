package xivvic.event;

import java.time.LocalTime;

public interface EventMetadata
{
	public String id();

	public String correlationId();

	public LocalTime publishedAt();
	

}
