package xivvic.command;


/**
 * Interface that indicates a component is able to generate commands.
 * 
 * @author Reid
 *
 */
public interface CommandGenerator
{
	/**
	 * Submit a command for processing. The command processor will be responsible
	 * for providing a unique id for this command.
	 * 
	 * @param command the command to execute.
	 * @return the unique ID for this command.
	 */
	String submit(Command command);

}
