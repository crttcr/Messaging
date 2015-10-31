package xivvic.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xivvic.util.concurrent.CompositeRunnable;
/**
 * Implements the CommandProcessor interface in a simple fashion.
 * 
 * DESIGN: How should this move from being reactive (only functions when called, e.g. receive(Cmd))
 * to being pro-active (Keeps a record of what has been executed and processes tasks when they complete)
 * 
 * @author Reid
 *
 */
public class CommandProcessorImpl
	implements CommandProcessor
{
	private final static Logger LOG = LoggerFactory.getLogger(CommandProcessorImpl.class.getName());

	/**
	 * This single thread pool exists to process commands when they have completed executed.
	 * 
	 */
	private final ScheduledExecutorService           harvest_thread = Executors.newScheduledThreadPool(1);
	
	/**
	 * This background processing pool executes the commands that are presented to this processor.
	 * Since they're executed in the background asynchronously, there needs to be some housekeeping
	 * action that occurs after the command is executed.  That runs in a separate pool.
	 */
	private final ExecutorCompletionService<CommandResult> executor;
	
	/**
	 * This provides CommandHandler objects the processor uses to handle each command
	 * 
	 */
	private final CommandHandlerFactory handler_factory;
	
	/**
	 * This provides a ResultProcessor object that can be scheduled to run in the harvesting thread.
	 * This is a factory method because this processor should have no knowledge of what is done by
	 * the environment where handlers execute commands.
	 * 
	 */
	private final ResultProcessorFactory publisher_factory;

	private final Map<String, Command>                 idmap = new HashMap<>();
	private final List<Command>                  submissions = new ArrayList<>();
	private boolean            terminated = false;
	private volatile boolean shuttingDown = false;
	
	public CommandProcessorImpl(CommandHandlerFactory ch_factory, ResultProcessorFactory rpf)
	{
		ExecutorService pool   = Executors.newCachedThreadPool();
		this.executor          = new ExecutorCompletionService<CommandResult>(pool);
		this.handler_factory   = ch_factory;
		this.publisher_factory = rpf;
		
		startResultHarvestingThread(executor);
	}
	
	@Override
	public CommandStatus receive(Command command)
	{
		if (shuttingDown || terminated)
			return CommandStatus.REJECTED;
		
		if (command == null)
		{
			String msg = String.format("null provided where valid command expected.  Ignore.");
			LOG.warn(msg);
			return CommandStatus.REJECTED;
		}
		
		if (command.status() != CommandStatus.CREATED)
		{
			String msg = String.format("Command not in CREATED state.  REJECTED.");
			LOG.warn(msg);
			command.setStatus(CommandStatus.REJECTED);
			return CommandStatus.REJECTED;
		}

		String id = command.id();
		if (id == null)
		{
			String msg = String.format("Commands cannot have null ID.  Ignore.");
			LOG.warn(msg);
			command.setStatus(CommandStatus.REJECTED);
			return CommandStatus.REJECTED;
		}
		
		boolean exists = idmap.containsKey(id);
		if (exists)
		{
			String msg = String.format("Command with ID [%s] already exists.  Reject.");
			LOG.warn(msg);
			command.setStatus(CommandStatus.REJECTED);
			return CommandStatus.REJECTED;
		}
		
		// At this point, we're confident that we will accept the command
		//
		if (dispatch(command))
		{
			command.setStatus(CommandStatus.ACKNOWLEDGED);
			submissions.add(command);
		}
		else
		{
			String msg = String.format("Failure to launch [%s].", command);
			LOG.warn(msg);
			return CommandStatus.REJECTED;
		}

		idmap.put(id, command);

		return CommandStatus.ACKNOWLEDGED;
	}

	@Override
	public boolean cancel(String id)
	{
		if (id == null)
			return false;
		
		Command c = idmap.get(id);
		if (c == null)
			return false;
		
		CommandStatus from = c.status();
		boolean      is_ok = c.setStatus(CommandStatus.CANCELLED);

		if (! is_ok)
		{
			String msg = String.format("Command [%s] cannot be cancelled from state [%s]", id, from);
			LOG.warn(msg);
		}
		
		return is_ok;
	}

//	@Override
//	public CommandResult awaitCompletion(String id)
//	{
//		if (id == null)
//		{
//			String msg = String.format("Looking up a null id is really pointless.");
//			LOG.warn(msg);
//			return null;
//		}
//
//		Future<CommandResult> future = futures.get(id);
//		if (future == null)
//		{
//			String msg = String.format("Failed to find future for id=[%s]", id);
//			LOG.warn(msg);
//			return null;
//		}
//		
//		while (! future.isDone())
//		{
//			try
//			{
//				Thread.sleep(50);
//			}
//			catch (InterruptedException e)
//			{
//				e.printStackTrace();
//			}
//			System.out.println("Yawn, sleepy.");
//		};
//		
//
//		CommandResult result = null;
//		try
//		{
//			result = future.get();
//		}
//		catch (InterruptedException | ExecutionException e)
//		{
//			e.printStackTrace();
//		}
//
//		return result;
//	}

	@Override
	public CommandStatus getStatusForCommand(String id)
	{
		if (id == null)
			return CommandStatus.UNKNOWN;
		
		Command c = idmap.get(id);
		if (c == null)
			return CommandStatus.UNKNOWN;
		
		
		return c.status();
	}


	public boolean dispatch(Command c)
	{
		String intent = c.intent();
		if (intent == null)
		{
			String msg = String.format("Refusing to dispatch command with no intent.");
			LOG.warn(msg);
			return false;
		}
		
		CommandHandler handler = handler_factory.handler(c);
		if (handler == null)
		{
			String msg = String.format("Failed to retrieve a handler for intent [%s]. No action.", intent);
			LOG.warn(msg);
			c.setStatus(CommandStatus.FAILED);
			return false;
		}
		
		executor.submit(handler);
		
		return true;
	}
	
	@Override
	public void shutdown()
	{
		/// TODO:  Make a shutdown command that will cause the thread pool to stop.
		///
		shuttingDown = true;
		
		harvest_thread.shutdown();
// 	queue.clear();
// 	queue.put(c);
	}

	@Override
	public List<Command> last(int n)
	{
		int size = submissions.size();
		
		if (size <= 0)
			return Command.EMPTY_LIST;
		
		int  end = n > size ? size : n;
		return submissions.subList(0, end);
	}

	private void startResultHarvestingThread(final ExecutorCompletionService<CommandResult> ecs)
	{
		final ResultProcessor publisher = publisher_factory.create();
		Runnable housekeeper = new Runnable()
		{
			public void run()
			{
   			try
   			{
   				final Future<CommandResult> future = ecs.take();
   				final CommandResult         result = future.get();
   
   				String    id = result.id();
   				Command  cmd = idmap.get(id);
   				if (cmd == null)
   				{
   					System.err.println("Commmand " + id + " missing.");
   					return;
   				}
   			
   				if (result.isSuccess())
   				{
   					cmd.setStatus(CommandStatus.COMPLETED);
   				}
   				else
   				{
   					cmd.setStatus(CommandStatus.FAILED);
   				}

   				publisher.setCommand(cmd);
   			}
   			catch(ExecutionException e)
   			{
   				String msg = String.format("Harvesting execution fail: [%s]", e.getLocalizedMessage());
   				System.err.println(msg);
   			}
   			catch (InterruptedException e)
   			{
   				String msg = String.format("Harvesting execution interrupt: [%s]", e.getLocalizedMessage());
   				System.err.println(msg);
   			}
			}
			
		};
		
		TimeUnit               unit = TimeUnit.MILLISECONDS;
		CompositeRunnable composite = new CompositeRunnable();

		composite.add(housekeeper);
		composite.add(publisher);
		harvest_thread.scheduleWithFixedDelay(composite, 100L, 50L, unit);
	}

}
