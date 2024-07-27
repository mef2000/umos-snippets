package ru.manku.desktop;

import snippets.threads.Threads;
import snippets.threads.parts.ThreadProvider;

public abstract class SwingJob extends ThreadProvider {
	//Do nothing class. As all modern programmicaly-culture >@.@<
	public SwingJob() { super(Threads.THREAD_UIABLE); }
	@Override public void progress(String id, float progress) {}
	@Override public void error(String id, String etag, Object packet) {}
	@Override public void event(String tag, Object packet) {}
	@Override public void begin() {}
	@Override public void end() { System.out.println("[SwingJob] Skipping, my life-cycle driving by SWING_UI_THREAD."); }
	@Override public boolean doing(String juid) { return true; }
	@Override public void cancel(String juid, boolean state) {}
}
