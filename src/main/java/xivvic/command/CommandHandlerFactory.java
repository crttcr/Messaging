package xivvic.command;

public interface CommandHandlerFactory
{
	CommandHandler handler(Command command);

}
