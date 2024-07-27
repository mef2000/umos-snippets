package coub.desktop.parts;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ru.manku.desktop.Bus;
import ru.manku.desktop.Launcher;
import snippets.matcher.parts.Reaction;

public class MainContent extends JPanel implements Reaction {
	private final Bus bus;
	private final Box hor = Box.createHorizontalBox(), vert = Box.createVerticalBox();
	private final ImageView preview = new ImageView();
	private final JLabel title = new JLabel(), creator = new JLabel(), stats = new JLabel();
	private final String[] values = new String[] {"Only Audio", "Only video", "Video by Audio Length", "Audio by Video Length"};
	private final JComboBox jopt = new JComboBox(values);
	private final JButton export = new JButton("export");
	
	public static final String ORGINIZE_EXPORT = "export";
	
	public static final int ONLY_AUDIO = 0;
	public static final int ONLY_VIDEO = 1;
	public static final int BY_AUDIO_LENGTH = 2;
	public static final int BY_VIDEO_LENGTH = 3;
	public MainContent(Bus bus) {
		this.bus = bus;
		
		export.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				bus.event(ORGINIZE_EXPORT, jopt.getSelectedIndex());
			}
		});
		Dimension d = new Dimension(256, 48);
		jopt.setMaximumSize(d);
		jopt.setSelectedIndex(0);
		Box hor2 = Box.createHorizontalBox();
		hor2.add(jopt);
		hor2.add(Box.createHorizontalStrut(5));
		hor2.add(export);
		
		vert.add(title);
		vert.add(creator);
		vert.add(stats);
		vert.add(hor2);
		preview.setTargetSize(640, 480);
		hor.add(preview);
		hor.add(Box.createHorizontalStrut(5));
		hor.add(vert);
		this.add(hor);
	}

	public void open(Coub c) {
		try {
			preview.setSource(Launcher.ROOT.concat(".cache/").concat(c.COUB_ID).concat(".jpg"));
			title.setText(c.DESC);
			creator.setText("by ".concat(c.AUTHOR));
			StringBuilder sb = new StringBuilder();
			sb.append("Views: ").append(c.VIEWS).append("; Likes: ")
				.append(c.LIKES).append(";\n Comments: ").append(c.CHAT_SIZE).append("; Category: ").append(c.CATEGORY);
			stats.setText(sb.toString());
		}catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(bus, e.toString(), "Error while showing coub", JOptionPane.ERROR_MESSAGE);
		}
	}
	@Override public void event(String tag, Object packet) {
		
	}
	@Override public void begin() {
		vert.setVisible(false);
		//preview.setVisible(false);
	}
	@Override public void end() {
		vert.setVisible(true);
		//preview.setVisible(true);
	}
	@Override public void progress(String id, float progress) {}
	@Override public void error(String id, String etag, Object packet) {}

}
