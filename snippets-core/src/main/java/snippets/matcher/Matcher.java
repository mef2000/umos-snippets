package snippets.matcher;

import snippets.matcher.parts.Cancellable;
import snippets.matcher.parts.Reaction;

public interface Matcher {
	public Object in();
	public Object out();
	public String juid();
	
	public Reaction reaction();
	public Cancellable cancelation();
}
