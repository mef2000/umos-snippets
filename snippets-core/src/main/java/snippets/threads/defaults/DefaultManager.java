package snippets.threads.defaults;

import java.util.HashMap;
import java.util.Map;

import snippets.threads.Threads;
import snippets.threads.parts.ThreadManager;
import snippets.threads.parts.ThreadProvider;

public class DefaultManager implements ThreadManager {
	private final HashMap<String, ThreadProvider> providers = new HashMap<>();
	
	public DefaultManager(ThreadProvider ui_support) {
		if(ui_support!=null) {
			if(!ui_support.THREAD_POLICY.equals(Threads.THREAD_UIABLE)) throw new RuntimeException("[DefaultManager] ThreadProvider for UI-jobs support not tagged as UIABLE, incoming thread policy"+ui_support.THREAD_POLICY);
			add(ui_support);
		}
		add(new InstantJob());
		add(new StackableJob());
		
	}
	public DefaultManager() {
		this(null);
		System.out.println("[DefaultManager] UI-jobs support is disabled - its maybe create 'Exceptionable' situations in future!");
	}
	@Override public void exit() {
		for(Map.Entry<String, ThreadProvider> thp : providers.entrySet()) thp.getValue().end();
		providers.clear();
	}
	@Override public void resume() {
		for(Map.Entry<String, ThreadProvider> thp : providers.entrySet()) thp.getValue().resume();
	}
	@Override public void pause() {
		for(Map.Entry<String, ThreadProvider> thp : providers.entrySet()) thp.getValue().pause();
	}
	@Override public void add(ThreadProvider provider) { providers.put(provider.THREAD_POLICY, provider); provider.begin(); }
	@Override public boolean exists(ThreadProvider provider) { return providers.containsKey(provider.THREAD_POLICY); }
	@Override public boolean exists(String thread_policy) { return providers.containsKey(thread_policy); }
	@Override public ThreadProvider get(String thread_policy) { return providers.get(thread_policy); }
}
