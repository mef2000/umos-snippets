package snippets.threads.parts;

import snippets.matcher.parts.Cancellable;
import snippets.matcher.parts.Reaction;
import snippets.threads.Job;

public abstract class ThreadProvider implements Reaction, Cancellable {
	public final String THREAD_POLICY;
	public ThreadProvider(String target_thread_policy) {
		THREAD_POLICY = target_thread_policy;
	}
	public abstract Job register(Job job);
	public void resume() {}
	public void pause() {}
}
