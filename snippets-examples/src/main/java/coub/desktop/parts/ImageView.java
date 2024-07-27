package coub.desktop.parts;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImageView extends JPanel {
	private int TWIDTH = 0, THEIGHT = 0;
	
	private BufferedImage image;
	private Image drawed;
	
	public void setTargetSize(int width, int height) {
		TWIDTH = width; THEIGHT = height;
		Dimension d = new Dimension(width, height);
		this.setSize(d);
		this.setPreferredSize(d);
		this.setMaximumSize(d);
		this.setMinimumSize(d);
	}
	
	public void setSource(String path) throws Exception {
		drawed = null;
		image = ImageIO.read(new File(path));
		int tw = TWIDTH, th = THEIGHT;
		if(image.getWidth()>image.getHeight()) {
			th = (int)(image.getHeight()*((1f*TWIDTH)/image.getWidth()));
		}else {
			tw = (int)(image.getWidth()*((1f*THEIGHT)/image.getHeight()));
		}
		drawed = image.getScaledInstance(tw, th, Image.SCALE_SMOOTH);
		repaint();
	}
	
	@Override public void paintComponent(Graphics gfx) {
		super.paintComponent(gfx);
		gfx.clearRect(0, 0, TWIDTH, THEIGHT);
		if(drawed != null) gfx.drawImage(drawed, 0, 0, null);
	}
}
