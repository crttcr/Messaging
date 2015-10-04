package xivvic.command;

import java.util.Map;

import com.google.auto.value.AutoValue;


@AutoValue
public abstract class CommandBase
	implements Command
{
	private CommandStatus status = CommandStatus.CREATED;
	
	/**
	 * Unique ID for this command
	 */
	@Override
	public abstract String id();
	
	/**
	 * String which identifies this command's specific intent.
	 * This can be used to identify different types of commands.
	 */
	@Override
	public abstract CommandSource source();


	public CommandStatus status()
	{
		return status;
	}

	public boolean setStatus(CommandStatus new_status)
	{
		if (new_status == null)
			return false;
		
		if (! CommandStatus.isOkTransition(status, new_status))
		{
			return false;
		}

		status = new_status;
		return true;
	}

	/**
	 * String which identifies this command's specific intent.
	 * This can be used to identify different types of commands.
	 */
	@Override
	public abstract String intent();

	/**
	 * Properties that are required for the command to be executed.
	 * 
	 * @return a map of properties that are to be used to complete the command
	 */
	@Override
	public abstract Map<String, Object> properties();

	
	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract CommandBase build();
		public abstract Builder id(String id);
		public abstract Builder source(CommandSource source);
		public abstract Builder intent(String intent);
		public abstract Builder properties(Map<String, Object> properties);
	}
	
	public static Builder builder()
	{
		return new AutoValue_CommandBase.Builder();
	}

}
