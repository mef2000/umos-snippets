package snippets.matcher.parts;

public interface Cancellable {
	public boolean doing(String juid);
	public void cancel(String juid, boolean state);
}
