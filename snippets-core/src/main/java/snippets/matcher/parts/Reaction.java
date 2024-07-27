package snippets.matcher.parts;

public interface Reaction {
	public default void write(String res_id, Object toWrite) { throw new RuntimeException("Method must be @Override!"); }
	public default Object read(String res_id) { throw new RuntimeException("Method must be @Override!"); }
	public default void progress(Object id, float progress) { throw new RuntimeException("Method must be @Override!"); }
	public default Object in() { return null; }
	public default Object out() { return null; }
	
	public void progress(String id, float progress);
	public void error(String id, String etag, Object packet);
	public void event(String tag, Object packet);
	
	public void begin();
	public void end();
}
