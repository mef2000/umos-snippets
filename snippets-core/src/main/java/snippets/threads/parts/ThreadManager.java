package snippets.threads.parts;

public interface ThreadManager {
	public default void exit() {}
	public default void enter() {}
	public default void pause() {}
	public default void resume() {}
	
	public boolean exists(ThreadProvider provider);
	public boolean exists(String thread_policy);
	public void add(ThreadProvider provider);
	public ThreadProvider get(String thread_policy);
}
