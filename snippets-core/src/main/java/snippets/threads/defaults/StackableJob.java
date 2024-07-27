package snippets.threads.defaults;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import snippets.threads.Job;
import snippets.threads.Threads;
import snippets.threads.parts.ThreadProvider;

public class StackableJob extends ThreadProvider implements Runnable {
	private volatile int STATE = Threads.STATE_LIFE;
	private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();
	private final Thread worker = new Thread(this);
	
	public StackableJob() { super(Threads.THREAD_STACKABLE); worker.start(); }
	
	@Override public void run() {
		while(STATE != Threads.STATE_KILL) try {
			if(STATE == Threads.STATE_LIFE) {
				if(jobs.size()<1) Thread.sleep(125);
				else for(Map.Entry<String, Job> j : jobs.entrySet()) {
					if(STATE == Threads.STATE_SLEEP) break;
					else {
						j.getValue().function.todo(j.getValue());
						jobs.remove(j.getKey());
					}
				}
			} else Thread.sleep(Threads.THREAD_SLEEP_MSEC);
		}catch(Exception e) { e.printStackTrace(); }
		System.out.println("[StackableJob] Thread killed!");
	}
	@Override public Job register(Job job) {
		if(jobs.containsKey(job.juid())) return jobs.get(job.juid());
		else {
			jobs.put(job.juid(), job);
			return job;
		}
	}

	@Override public void cancel(String juid, boolean state) { jobs.remove(juid); }
	@Override public boolean doing(String juid) { return jobs.containsKey(juid); }
	@Override public void pause() { STATE = Threads.STATE_SLEEP; }
	@Override public void resume() { STATE = Threads.STATE_LIFE; }
	@Override public void begin() { resume(); }
	@Override public void end() { jobs.clear(); STATE = Threads.STATE_KILL; }
	
	//Not used methods from Reaction, Cancellable
	@Override public void progress(String id, float progress) {}
	@Override public void error(String id, String etag, Object packet) {}
	@Override public void event(String tag, Object packet) {}
}
