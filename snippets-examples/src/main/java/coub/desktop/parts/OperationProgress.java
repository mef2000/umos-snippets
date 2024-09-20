package coub.desktop.parts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ru.manku.desktop.Bus;
import snippets.Snippet;
import snippets.matcher.Matcher;
import snippets.matcher.parts.Cancellable;
import snippets.matcher.parts.Reaction;
import static snippets.threads.Threads.*;

public class OperationProgress extends JPanel implements Reaction, Cancellable {
	public static final String CANCEL_SEARCH = "cancel_job";
	private volatile boolean isLife = false;
	private final Bus bus;
	private final Box cont = Box.createHorizontalBox();
	private final JButton cancel = new JButton("cancel");
	private final JProgressBar jstatus = new JProgressBar(0, 100);
	public OperationProgress(Bus bus) {
		this.bus = bus;
		
		jstatus.setStringPainted(true);
		
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				isLife = false;
				bus.event(CANCEL_SEARCH, null);
			}});
		cont.add(jstatus);
		cont.add(Box.createHorizontalStrut(10));
		cont.add(cancel);
		this.add(cont);
	}
	
	private void update(Matcher s) { //, Object in, Object out, Reaction react, Cancellable cancel) {
		jstatus.setValue((Integer)s.in());
	}

	
	@Override public void progress(String id, float progress) {
		//Swing support concurrent-thread changes UI, but Android throws Exception
		//Lets use launch(Snippet for UI-threads), to write universal-code.
		launchUI(this::update, (int)(100*progress));
		
		//launch(THREAD_UIABLE, (int)(100*progress), null, null, null, this::update);
	
	}
	//Ignoring - just send to Bus Main
	@Override public void error(String id, String etag, Object packet) { bus.error(id, etag, packet);}

	@Override public void begin() {
		isLife = true;
		jstatus.setValue(0);
		cancel.setEnabled(true);
	}

	@Override public void end() {
		isLife = false;
		cancel.setEnabled(false);
		jstatus.setValue(100);
	}
	
	//Working with one JOB - ignoring JOB_UID
	@Override public boolean doing(String juid) { return isLife; }
	@Override public void cancel(String juid, boolean lifeState) { isLife = !lifeState; }
	
	@Override public void event(String tag, Object packet) {}
}
