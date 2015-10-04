package xivvic.command;

public class CommandResult
{
	private final String id;
	private final boolean success;
	private final String  message;

	CommandResult (String id, boolean success, String message)
	{
		this.id      = id;
		this.success = success;
		this.message = message;
	}

	public static CommandResult success(String id)
	{
		CommandResult cr = new CommandResult(id, true, null);
		return cr;
	}

	public static CommandResult failure(String id, String msg)
	{
		CommandResult cr = new CommandResult(id, false, msg);
		return cr;
	}
	
	public String id()
	{
		return id;
	}
		
	public boolean isSuccess()
	{
		return success;
	}
	
	public String message()
	{
		return message;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		String       msg = null;
		
		if (isSuccess())
		{
			msg = String.format("CR[%s]: Success.", id);
		}
		else
		{
			msg = String.format("CR[%s]: Failure - %s", id, message);
		}
		
		sb.append(msg);
		
		return sb.toString();
	}
}
