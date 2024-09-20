package snippets.threads;

import java.util.UUID;

import snippets.Snippet;
import snippets.matcher.parts.Cancellable;
import snippets.matcher.parts.Reaction;
import snippets.threads.parts.ThreadManager;
import snippets.threads.parts.ThreadProvider;

public class Threads {
	public static final String THREAD_STACKABLE = "stack_0";
	public static final String THREAD_UIABLE = "runOnUi";
	public static final String THREAD_INSTANTABLE = "instanlty";
	
	public static final int STATE_KILL = -1;
	public static final int STATE_LIFE = 0;
	public static final int STATE_SLEEP = 1;
	
	private static ThreadManager tman = null;
	
	public static long THREAD_SLEEP_MSEC = 250;
	
	private static String DEFAULT_POLICY = null;
	
	public static void defaultPolicy(String policy) {
		if(!tman.exists(policy)) throw new RuntimeException("[Threads] Trying to access unexists ThreadProvider for policy: "+policy);
		DEFAULT_POLICY = policy;
	}
	
	public static String policy() { return DEFAULT_POLICY; }
	
	public static void link(ThreadManager manager) {
		if(manager == null) throw new RuntimeException("[Threads] Manager must be life-object, to null-reference, use Threads.unlink()");
		tman = manager;
		tman.enter();
	}
	public static void unlink() {
		tman.exit();
		tman = null;
	}
	
	public static Job launch(String THREAD_POLICY, String JUID, Object in, Object out, Reaction reaction, Cancellable cancel, Snippet function) {
		if(!tman.exists(THREAD_POLICY)) throw new RuntimeException("[Threads] Unknown policy, please connect ThreadProvider before using target_policy: "+THREAD_POLICY);
		return tman.get(THREAD_POLICY).register(new Job(JUID, in, out, reaction, cancel, function));
	}
	
	public static Job launch(String THREAD_POLICY, Object in, Object out, Reaction reaction, Cancellable cancel, Snippet function) {
		return launch(THREAD_POLICY, UUID.randomUUID().toString(), in, out, reaction, cancel, function);
	}
	
	public static Job launch(String THREAD_POLICY, Reaction reaction, Cancellable cancel, Snippet function) {
		return launch(THREAD_POLICY, reaction.in(), reaction.out(), reaction, cancel, function);
	}
	
	public static Job launch(String THREAD_POLICY, Object in, Object out, Reaction reaction, Snippet function) {
		if(!tman.exists(THREAD_POLICY)) throw new RuntimeException("[Threads] Unknown policy, please connect ThreadProvider before using target_policy: "+THREAD_POLICY);
		ThreadProvider bridge = tman.get(THREAD_POLICY);
		return launch(THREAD_POLICY, in, out, reaction, bridge, function);
	}
	
	public static Job launch(String THREAD_POLICY, Reaction reaction, Snippet function) {
		if(!tman.exists(THREAD_POLICY)) throw new RuntimeException("[Threads] Unknown policy, please connect ThreadProvider before using target_policy: "+THREAD_POLICY);
		ThreadProvider bridge = tman.get(THREAD_POLICY);
		return launch(THREAD_POLICY, reaction.in(), reaction.out(), reaction, bridge, function);
	}
	
	public static Job launch(String THREAD_POLICY, Snippet function) {
		if(!tman.exists(THREAD_POLICY)) throw new RuntimeException("[Threads] Unknown policy, please connect ThreadProvider before using target_policy: "+THREAD_POLICY);
		ThreadProvider bridge = tman.get(THREAD_POLICY);
		return launch(THREAD_POLICY, bridge.in(), bridge.out(), bridge,  bridge, function);
	}
	
	public static Job launch(String THREAD_POLICY, Object in, Object out, Snippet function) {
		if(!tman.exists(THREAD_POLICY)) throw new RuntimeException("[Threads] Unknown policy, please connect ThreadProvider before using target_policy: "+THREAD_POLICY);
		ThreadProvider bridge = tman.get(THREAD_POLICY);
		return launch(THREAD_POLICY, in, out, bridge, bridge, function);
	}
	
	public static Job launch(String THREAD_POLICY, Object in, Reaction reaction,  Snippet function) {
		if(!tman.exists(THREAD_POLICY)) throw new RuntimeException("[Threads] Unknown policy, please connect ThreadProvider before using target_policy: "+THREAD_POLICY);
		ThreadProvider bridge = tman.get(THREAD_POLICY);
		return launch(THREAD_POLICY, in, reaction.out(), reaction, bridge, function);
	}	
	
	public static Job launch(String THREAD_POLICY, Object in, Snippet function) {
		if(!tman.exists(THREAD_POLICY)) throw new RuntimeException("[Threads] Unknown policy, please connect ThreadProvider before using target_policy: "+THREAD_POLICY);
		ThreadProvider bridge = tman.get(THREAD_POLICY);
		return launch(THREAD_POLICY, in, bridge.out(), bridge, bridge, function);
	}	
	public static Job launch(Snippet function) {
		return launch(DEFAULT_POLICY, function);
	}
	
	public static Job launch(String THREAD_POLICY, Snippet function, Object... iorc) {
		if(iorc==null) throw new RuntimeException("[Threads] Bad IORC-word, NPE-input data!");
		switch(iorc.length) {
			default: throw new RuntimeException("[Threads] Wrong IORC-pool, Unprocessable data!");
			case 0: return launch(THREAD_POLICY, function);
			case 1: 
				if(iorc[0] instanceof Reaction) return launch(THREAD_POLICY, (Reaction)iorc[0], function);
				else return launch(THREAD_POLICY, iorc[0], function); 
			case 2:
				boolean p1 = iorc[0] instanceof Reaction;
				boolean p2 = iorc[1] instanceof Cancellable;
				boolean p3 = iorc[1] instanceof Reaction;
				if( p1 && p2 ) return launch(THREAD_POLICY, (Reaction)iorc[0], (Cancellable)iorc[1], function);
				else if(p3) return launch(THREAD_POLICY, iorc[0], (Reaction)iorc[1], function);
				else return launch(THREAD_POLICY, iorc[0], iorc[1], function);
			case 3: 
				boolean p4 = iorc[2] instanceof Reaction;
				// 0: in 1: out 2: reaction
				if(p4) return launch(THREAD_POLICY, iorc[0], iorc[1], (Reaction)iorc[2], function);
				// 0: in 1: reaction 2: cancellable
				else {
					Reaction r = (Reaction)iorc[1];
					return launch(THREAD_POLICY, iorc[0], r.out(), r, (Cancellable)iorc[2], function);
				}
			case 4: return launch(THREAD_POLICY, iorc[0], iorc[1], (Reaction)iorc[2], (Cancellable)iorc[3], function);
		}
	}
	
	public static Job launchUI(Snippet function, Object... iorc) {
		return launch(THREAD_UIABLE, function, iorc);
	}
	public static Job launchNow(Snippet function, Object... iorc) {
		return launch(THREAD_INSTANTABLE, function, iorc);
	}
	public static Job launchStack(Snippet function, Object... iorc) {
		return launch(THREAD_STACKABLE, function, iorc);
	}
	
	public static void resume() {
		if(tman == null) throw new RuntimeException("[Threads] Manager must be life-object, before this method set ThreadManager by link(...) method.");
		tman.pause();
	}
	
	public static void pause() {
		if(tman == null) throw new RuntimeException("[Threads] Manager must be life-object, before this method set ThreadManager by link(...) method.");
		tman.resume();
	}
	

}
