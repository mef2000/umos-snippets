package snippets.threads;

import snippets.Snippet;
import snippets.matcher.Matcher;
import snippets.matcher.parts.Cancellable;
import snippets.matcher.parts.Reaction;

public class Job implements Matcher {
	private final Object in, out;
	private final String JUID;
	private final Reaction reaction;
	private final Cancellable cancel;
	
	public final Snippet function;
	
	public Job(String juid, Object in, Object out, Reaction r, Cancellable c, Snippet function) {
		this.in = in; this.out = out;
		JUID = juid; this.function = function;
		this.reaction = r; this.cancel = c;
	}
	
	@Override public Object in() { return in; }
	@Override public Object out() { return out; }
	@Override public String juid() { return JUID; }
	@Override public Reaction reaction() { return reaction; }
	@Override public Cancellable cancelation() { return cancel; }
	
}
