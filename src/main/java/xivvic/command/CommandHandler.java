package xivvic.command;

import java.util.concurrent.Callable;

public interface CommandHandler
	extends Callable<CommandResult>
{
}
