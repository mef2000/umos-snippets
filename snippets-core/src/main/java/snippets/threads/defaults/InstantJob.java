package snippets.threads.defaults;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import snippets.threads.Job;
import snippets.threads.Threads;
import snippets.threads.parts.ThreadProvider;

public class InstantJob extends ThreadProvider {
	public InstantJob() { super(Threads.THREAD_INSTANTABLE); }
	private final ExecutorService threads = Executors.newCachedThreadPool();
	
	@Override public Job register(Job job) {
		threads.execute(job.function);
		return job;
	}
	
	@Override public void end() { threads.shutdown(); System.out.println("[InstantJob] EXIT_SIG sended to ExecutorService"); }
	@Override public void cancel(String juid, boolean state) { System.out.println("[InstantJob] ThreadProvider dont support cancellable-state. Ignoring access for "+juid);	}
	@Override public boolean doing(String juid) {
		System.out.println("[InstantJob] ThreadProvider dont support cancellable-state. Ignoring access for "+juid);
		return true;
	}

	//Not used methods from Reaction, Cancellable
	@Override public void begin() {}
	@Override public void progress(String id, float progress) {}
	@Override public void error(String id, String etag, Object packet) {}
	@Override public void event(String tag, Object packet) {}
}
