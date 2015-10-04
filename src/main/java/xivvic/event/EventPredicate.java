package xivvic.event;

@FunctionalInterface
public interface EventPredicate
{
	EventPredicate TRUE = new EventPredicate() 
	{ 
		public boolean test(Event e) { return true; }
	};
	

	public boolean test(Event e);

}
