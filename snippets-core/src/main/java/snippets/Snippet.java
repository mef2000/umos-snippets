package snippets;

import snippets.matcher.Matcher;

public abstract class Snippet implements Runnable {
	protected Matcher internal = null;
	public String TARGET_POLICY = null;
	public Snippet(String target_policy) {
		TARGET_POLICY = target_policy;
		if(TARGET_POLICY == null) throw new RuntimeException("[Snippets] ThreadPolicy must be real-object.");
	}
	@Override public void run() {
		todo(internal);
	}
	public abstract void todo(Matcher s);
	public void match(Matcher direct) { this.internal = direct; }
}
