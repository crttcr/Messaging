package xivvic.command;

public interface ResultProcessor
	extends Runnable
{
	void setCommand(Command cmd);
}
