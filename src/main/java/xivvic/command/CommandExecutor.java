package xivvic.command;


/**
 * This interface is for the object that actually tries to execute a command.
 * The {@link CommandProcessor} is responsible for all the ancillary housekeeping
 * details around commands, but this interface represents the actual doer.
 * 
 * @author Reid
 */
public interface CommandExecutor
{
	CommandResult execute(Command command);
}
