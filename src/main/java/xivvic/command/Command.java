package xivvic.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Command
{
	List<Command> EMPTY_LIST = new ArrayList<>(0);

	/**
	 * A unique id for the command.
	 * @return
	 */
	String id();
	
	/**
	 * The source of the command
	 * @return the originator of the command.
	 */
	CommandSource source();
	
	/**
	 * Returns the status of this command.
	 */
	CommandStatus status();

	/**
	 * Attempts to set the new status for this command.
	 * If successful, returns true, otherwise false.
	 * 
	 * See {@link CommandStatus.isOkTransition()} for conditions controlling
	 * state changes
	 * 
	 * @return true when the status is updated, false when the new status is rejected.
	 */
	boolean setStatus(CommandStatus status);

	/**
	 * String which identifies this command's specific intent.
	 * This can be used to identify different types of commands.
	 */
	String intent();

	/**
	 * Properties that are required for the command to be executed.
	 * 
	 * @return a map of properties that are to be used to complete the command
	 */
	Map<String, Object> properties();

}
