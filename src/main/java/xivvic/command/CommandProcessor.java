package xivvic.command;

import java.util.List;



public interface CommandProcessor
{
	/**
	 * Receives a command for processing. If the command is successful, then this
	 * method returns an ID representing this command.  If the command is rejected,
	 * then it will return null.
	 * 
	 * @param command the command to process
	 * @return returns an ID which can be used to identify the submitted command, or null if the command is rejected for any reason.
	 */
	CommandStatus receive(Command command); 

	boolean cancel(String id);
	
	CommandStatus getStatusForCommand(String id);

	/**
	 * Causes the command processor to terminate.
	 * Once terminated, this processor will never restart.
	 */
	void shutdown();

	/**
	 * Returns the last n commands received
	 * 
	 */
	List<Command> last(int n);
}
