package xivvic.command;

public enum CommandStatus
{
	CREATED,          // The command was created, but not yet acknowledged
	ACKNOWLEDGED,     // The command was accepted for processing
	REJECTED,         // The command was not valid or acceptable to the command processor
	CANCELLED,        // The command was cancelled
	COMPLETED,        // The command was successfully executed
	FAILED,           // The command processing was not successful
	UNKNOWN, 			// The status is not known (e.g. A processor receives a status request for an unseen command)
	;

	public static boolean isOkTransition(CommandStatus from, CommandStatus to)
	{
		// Can't go back to created status.
		//
		if (to == CREATED)
			return false;
		
		// Can't leave terminal states
		//
		if (from == REJECTED || from == CANCELLED || from == FAILED || from == COMPLETED)
			return false;
		
		// Can't suddenly start working from unknown state
		//
		if (from == UNKNOWN)
			return false;
		
		// From created, next state is ack / nack only
		//
		if (from == CREATED)
		{
			if (to == ACKNOWLEDGED || to == REJECTED) // Don't think I need this case: || to == CANCELLED)
				return true;
			
			return false;
		}
		
		// From ACKNOWLEDGED, next state is ack / nack only
		//
		if (from == ACKNOWLEDGED)
		{
			if (to == COMPLETED || to == FAILED || to == CANCELLED)
				return true;
			
			return false;
		}

		// Any other case is disallowed, unless there's a case where we need to reconsider
		// this logic.
		return false;
		}
}