package snippets.threads;

import java.util.UUID;

import snippets.Snippet;
import snippets.matcher.parts.Cancellable;
import snippets.matcher.parts.Reaction;
import snippets.threads.parts.ThreadManager;

public class Threads {
	public static final String THREAD_STACKABLE = "stack_0";
	public static final String THREAD_UIABLE = "runOnUi";
	public static final String THREAD_INSTANTABLE = "instanlty";
	
	public static final int STATE_KILL = -1;
	public static final int STATE_LIFE = 0;
	public static final int STATE_SLEEP = 1;
	
	private static ThreadManager tman = null;
	
	public static long THREAD_SLEEP_MSEC = 250;
	public static void link(ThreadManager manager) {
		if(manager == null) throw new RuntimeException("[Threads] Manager must be life-object, to null-reference, use Threads.unlink()");
		tman = manager;
		tman.enter();
	}
	public static void unlink() {
		tman.exit();
		tman = null;
	}
	public static Job launch(String custom_JUID, Object in, Object out, Reaction reaction, Cancellable cancel, Snippet function) {
		if(tman == null) throw new RuntimeException("[Threads] Manager must be life-object, before this method set ThreadManager by link(...) method.");
		else if(!tman.exists(function.TARGET_POLICY)) throw new RuntimeException("[Threads] Unknown policy, please connect ThreadProvider before using target_policy: "+function.TARGET_POLICY);
		return tman.get(function.TARGET_POLICY).register(new Job(custom_JUID, in, out, reaction, cancel, function));
	}
	public static Job launch(Object in, Object out, Reaction reaction, Cancellable cancel, Snippet function) {
		return launch(UUID.randomUUID().toString(), in, out, reaction, cancel, function);
	}
	public static Job launch(Reaction reaction, Cancellable cancel, Snippet function) {
		return launch(reaction.in(), reaction.out(), reaction, cancel, function);
	}
	public static Job launch(Reaction reaction, Snippet function) {
		if(tman == null) throw new RuntimeException("[Threads] Manager must be life-object, before this method set ThreadManager by link(...) method.");
		else if(!tman.exists(function.TARGET_POLICY)) throw new RuntimeException("[Threads] Unknown policy, please connect ThreadProvider before using target_policy: "+function.TARGET_POLICY);
		return launch(reaction, tman.get(function.TARGET_POLICY), function);
	}
	public static Job launch(Object in, Object out, Reaction reaction, Snippet function) {
		if(tman == null) throw new RuntimeException("[Threads] Manager must be life-object, before this method set ThreadManager by link(...) method.");
		else if(!tman.exists(function.TARGET_POLICY)) throw new RuntimeException("[Threads] Unknown policy, please connect ThreadProvider before using target_policy: "+function.TARGET_POLICY);
		return launch(in, out, reaction, tman.get(function.TARGET_POLICY), function);
	}
	public static Job launch(Object in, Object out, Snippet function) {
		if(tman == null) throw new RuntimeException("[Threads] Manager must be life-object, before this method set ThreadManager by link(...) method.");
		else if(!tman.exists(function.TARGET_POLICY)) throw new RuntimeException("[Threads] Unknown policy, please connect ThreadProvider before using target_policy: "+function.TARGET_POLICY);
		return launch(in, out, tman.get(function.TARGET_POLICY), tman.get(function.TARGET_POLICY), function);
	}
	public static Job launch(Snippet function) {
		if(tman == null) throw new RuntimeException("[Threads] Manager must be life-object, before this method set ThreadManager by link(...) method.");
		else if(!tman.exists(function.TARGET_POLICY)) throw new RuntimeException("[Threads] Unknown policy, please connect ThreadProvider before using target_policy: "+function.TARGET_POLICY);
		return launch(tman.get(function.TARGET_POLICY), tman.get(function.TARGET_POLICY), function);
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
