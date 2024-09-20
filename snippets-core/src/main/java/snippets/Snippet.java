package snippets;

import snippets.matcher.Matcher;
import snippets.matcher.parts.Cancellable;
import snippets.matcher.parts.Reaction;

@FunctionalInterface
public interface Snippet extends Runnable {
	public void work(Matcher iorc);//, Object in, Object out, Reaction r, Cancellable cancel);//Object in, Object out, Reaction r, Cancellable cancel);
	
	public default Matcher extract() { return null; }
	
	@Override public default void run() {
		if(extract()==null) throw new RuntimeException("[Snippet] Wrong self-calling Snippet by run(), 'Matcher' isn't real object (NullPointer). You @Override extract()-function?");
		Matcher m = extract();
		work(m);//, m.in(), m.out(), m.reaction(), m.cancelation());//m.in(), m.out(), m.reaction(), m.cancelation());
	} 
}
