package snippets.matcher.parts;

public interface Errorable {
	public void error(String event, Object res_id, Object error);
	public default void exception(String event, Object res_id, Exception body) { throw new RuntimeException("[Errorable] Method isn't @Overriden!"); }
}
