package coub.desktop.parts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ru.manku.desktop.Bus;
import snippets.matcher.parts.Reaction;

public class LinkSearcher extends JPanel implements Reaction {
	public static final String BEGIN_SEARCH = "START_SEARCH";
	
	private final Bus bus;
	private final Box cont = Box.createHorizontalBox();
	private final JButton search = new JButton("search");
	private final JTextField edit = new JTextField(70);
	private final JLabel label = new JLabel("Enter coub-share link: ");
	public LinkSearcher(Bus bus) {
		this.bus = bus;
		
		search.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				String s = edit.getText();
				try{
					if(!s.startsWith("https://coub.com/view/")) throw new RuntimeException("Illegal coub share URL: "+s);
					s = s.substring(s.lastIndexOf("/")+1);
					if(s.length()!=6) throw new RuntimeException("Illegal coub share ID: "+s);
					bus.event(BEGIN_SEARCH, s);
				}catch(Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(bus, e.toString(), "Error while search coub", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		cont.add(label);
		cont.add(edit);
		cont.add(Box.createHorizontalStrut(10));
		cont.add(search);
		this.add(cont);
	}
	
	@Override public void begin() { unlock(false); }
	@Override public void end() { unlock(true); }
	
	protected void unlock(boolean state) {
		edit.setEnabled(state);
		search.setEnabled(state);
	}
	
	@Override
	public void progress(String id, float progress) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void error(String id, String etag, Object packet) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void event(String tag, Object packet) {
		// TODO Auto-generated method stub
		
	}

}
