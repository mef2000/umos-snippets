package ru.manku.desktop;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import coub.desktop.parts.Coub;
import coub.desktop.parts.LinkSearcher;
import coub.desktop.parts.MainContent;
import coub.desktop.parts.OperationProgress;
import snippets.Snippet;
import snippets.matcher.Matcher;
import snippets.matcher.parts.Cancellable;
import snippets.matcher.parts.Reaction;
import static snippets.threads.Threads.*;

import snippets.threads.Threads;
import snippets.threads.parts.ThreadManager;

public class Bus extends JFrame implements WindowListener, Reaction {
	public static final String ERROR_LOADING = "error_onload";
	public static final String ERROR_EXPORT = "error_export";
	public static final String DONE_LOADING = "done_onload";
	public static final String EXPORT_DONE = "done_export";
	public static final String NO_FFMPEG_SUPPORT = "no_ffmpeg";

	private final ThreadManager TMAN;
	
	private final OperationProgress status = new OperationProgress(this);
	private final LinkSearcher search = new LinkSearcher(this);
	private final MainContent main = new MainContent(this);
	public Bus(ThreadManager thrman) {
		super("Simple Coub File Downloader :: PB20240726/1G0");
		TMAN = thrman;
		
		this.setSize(1280, 720);
		this.getContentPane().add(BorderLayout.NORTH, search);
		this.getContentPane().add(BorderLayout.SOUTH, status);
		this.getContentPane().add(BorderLayout.CENTER, main);
		this.addWindowListener(this);
		this.setVisible(true);
		end();
		main.begin();
	
	}
	@Override public void progress(String id, float progress) { status.progress(id, progress); }
	@Override public void error(String id, String etag, Object packet) {
		if(ERROR_LOADING.equals(etag)) {
			JOptionPane.showMessageDialog(this, "Cannot create local coub-cache: "+packet, "Error while caching coub", JOptionPane.ERROR_MESSAGE);
		}else if(ERROR_EXPORT.equals(etag)) {
			JOptionPane.showMessageDialog(this, "Cannot Export coub from cache with error: "+packet, "Error while exporting coub", JOptionPane.ERROR_MESSAGE);
		}else if(NO_FFMPEG_SUPPORT.equals(etag)) {
			JOptionPane.showMessageDialog(this, "Please check ffmpeg file is exists  in directory: "+packet, "FFMPEG Library Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private Coub coub;
	@Override public void event(String tag, Object packet) {
		if(LinkSearcher.BEGIN_SEARCH.equals(tag)) {
			//Okey, we will directly drive the async-life, dont delegate behavior to ThreadProvider
			status.cancel(null, false);
			launchStack(Bus::getInfo, packet, this, status);
		}else if(DONE_LOADING.equals(tag)) {
			coub = (Coub)packet;
			main.end();
			main.open(coub);
		}else if(MainContent.ORGINIZE_EXPORT.equals(tag)) {
			final int id = (Integer)packet;
			if(coub != null) switch(id) {
			case MainContent.ONLY_AUDIO:
				launchStack(Bus::copyJob,
							Launcher.ROOT.concat(".cache/").concat(coub.COUB_ID).concat(".mp3"),
							Launcher.ROOT.concat("exported/").concat("exported_JOB_"+coub.COUB_ID).concat(".mp3"),
							this);
				break;
			case MainContent.ONLY_VIDEO:
				launchStack(Bus::copyJob,
						Launcher.ROOT.concat(".cache/").concat(coub.COUB_ID).concat(".mp4"),
						Launcher.ROOT.concat("exported/").concat("exported_JOB_"+coub.COUB_ID).concat(".mp4"),
						this);
				break;
			case MainContent.BY_AUDIO_LENGTH:
				if(!coub.AUDIO.equals("not_found")) {
					status.cancel(null, false);
					launchStack(Bus::runFFMPEG, coub, "audio", this, status);
				}
				else JOptionPane.showMessageDialog(this, "This coub dont have audio track. Ignoring..", "Broken coub", JOptionPane.ERROR_MESSAGE);
				break;
			case MainContent.BY_VIDEO_LENGTH:
				if(!coub.AUDIO.equals("not_found")) {
					status.cancel(null, false);
					launchStack(Bus::runFFMPEG, coub, "video", this, status);
				}
				else JOptionPane.showMessageDialog(this, "This coub dont have audio track. Ignoring..", "Broken coub", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}else if(EXPORT_DONE.equals(tag)) {
			main.end();
		}
	}
	
	private static void runFFMPEG(Matcher s) { //, Object in, Object out, Reaction eventer, Cancellable cancel) {
		final Reaction eventer = s.reaction();
		final Cancellable cancel = s.cancelation();
		String os = System.getProperty("os.name").toLowerCase();
		boolean isWin = os.contains("win");
		String lib = "";
		if(isWin) lib = Launcher.ROOT.concat("ffmpeg.exe");
		else lib = Launcher.ROOT.concat("ffmpeg");
		if(!(new File(lib).exists())) {
			eventer.error(s.juid(), NO_FFMPEG_SUPPORT, lib);
			return;
		}
		eventer.progress(s.juid(), 0.1f);
		final Coub c = (Coub)s.in();
		final String type = (String)s.out();
		String[] cmdz = null;
		if("video".equals(type)) {
			cmdz = new String[] {Launcher.ROOT.concat("ffmpeg"), "-i", Launcher.ROOT.concat(".cache/").concat(c.COUB_ID).concat(".mp4"),
								"-i", Launcher.ROOT.concat(".cache/").concat(c.COUB_ID).concat(".mp3"),
								"-c:v", "copy", "-c:a", "aac",
								"-shortest", "-nostdin", "-v", "quiet", Launcher.ROOT.concat("exported/").concat("video_LENGTH_"+c.COUB_ID).concat(".mp4")
								};
		}else {
			cmdz = new String[] {Launcher.ROOT.concat("ffmpeg"), "-stream_loop", "-1",
								"-i", Launcher.ROOT.concat(".cache/").concat(c.COUB_ID).concat(".mp4"),
								"-i", Launcher.ROOT.concat(".cache/").concat(c.COUB_ID).concat(".mp3"),
								"-map", "0:v:0", "-map", "1:a:0", "-c:v", "copy", "-c:a", "aac",
								"-shortest", "-nostdin", "-v", "quiet", Launcher.ROOT.concat("exported/").concat("audio_LENGTH_"+c.COUB_ID).concat(".mp4")
								};
		}
		eventer.begin();
		eventer.progress(s.juid(), 0.25f);
		Process rt;
		try {
			rt = Runtime.getRuntime().exec(cmdz);
			while(rt.isAlive()) {
				Thread.sleep(200);
				if(!cancel.doing(s.juid())) rt.destroy();
			}
			eventer.progress(s.juid(), 1f);
			eventer.event(EXPORT_DONE, null);
		} catch (Exception e) {
			e.printStackTrace();
			eventer.progress(s.juid(), 0f);
			eventer.error(s.juid(), ERROR_EXPORT, e.toString());
		}
		eventer.end();
	}

	public static void copyJob(Matcher s) { // , Object in, Object out, Reaction eventer, Cancellable cancel) {
		final String from = (String)s.in();
		final String to = (String)s.out();
		try {
			Files.copy(Paths.get(from), Paths.get(to), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
			s.reaction().error(s.juid(), ERROR_EXPORT, e.toString());
		}
	}
	
	
	private static void getInfo(Matcher s) { //, Object in, Object out, Reaction eventer, Cancellable job) {
		final String target = (String)s.in();
		final Reaction eventer = s.reaction();
		final Cancellable job = s.cancelation();
		eventer.begin();
		try {
			//Drafts code, its need to export Coub API Library
			JSONObject body = (JSONObject) new JSONParser().parse(loadJSON("https://coub.com/api/v2/coubs/"+target));
			Coub c = new Coub();
			c.DESC = (String)body.getOrDefault("title", "not_found");
            c.COUB_ID = (String)body.get("permalink");
            c.VIEWS = Integer.valueOf(body.get("views_count").toString());
            c.LIKES = Integer.valueOf(body.get("likes_count").toString());
            c.CHAT_SIZE = Integer.valueOf(body.get("comments_count").toString());
            String[] d = ((String)body.get("created_at")).split("T")[0].split("-");
            c.DATE =  d[2]+"/"+d[1]+"/"+d[0];
            try {
                JSONObject music = (JSONObject) body.get("music");
                String title = (String) body.get("title");
                String author = (String) body.get("artist_title");
                c.MUSIC_NAME = "".concat(author).concat(" - ").concat(title);
            }catch(Exception e) {
                c.MUSIC_NAME = "";
            }
            try{
                c.CATEGORY = "#"+((JSONObject)((JSONArray)body.get("categories")).get(0)).get("title");
                JSONArray tags = (JSONArray) body.get("tags");
                for(int z=0; z<tags.size(); z++) {
                    JSONObject tag = (JSONObject) tags.get(z);
                    c.TAGS.add("#"+tag.get("title"));
                }
            }catch(Exception e) { c.CATEGORY = "#>@.@<?"; }
            JSONObject files = (JSONObject)((JSONObject)body.get("file_versions")).get("html5");
            c.VIDEO = (String)((JSONObject)((JSONObject)files.get("video")).get("high")).get("url");
            c.CHANNEL_ID = (Long)((JSONObject)body.get("channel")).get("id");
            c.PREVIEW = ((String)((JSONObject)body.get("image_versions")).get("template")).replace("%{version}", "med");
            c.SERVER_ID = (Long)body.get("id");
            
            JSONObject chan = (JSONObject) body.get("channel");
            c.ICON = ((String)((JSONObject)chan.get("avatar_versions")).get("template")).replace("%{version}", "profile_pic");
            c.AUTHOR = (String) chan.get("title");
      
             try {
            	c.AUDIO = (String) ((JSONObject)((JSONObject)files.get("audio")).get("med")).get("url");
            }catch(Exception e) {
            	c.AUDIO = "not_found";
            }
            if(job.doing(s.juid())) saveBytes(c.ICON, Launcher.ROOT.concat(".cache/").concat(""+c.CHANNEL_ID).concat(".jpg"), eventer);
            if(job.doing(s.juid())) saveBytes(c.PREVIEW, Launcher.ROOT.concat(".cache/").concat(c.COUB_ID).concat(".jpg"), eventer);
            if((job.doing(s.juid()))&&(!"not_found".equals(c.AUDIO))) saveBytes(c.AUDIO, Launcher.ROOT.concat(".cache/").concat(c.COUB_ID).concat(".mp3"), eventer);
            if(job.doing(s.juid())) saveBytes(c.VIDEO, Launcher.ROOT.concat(".cache/").concat(c.COUB_ID).concat(".mp4"), eventer);
            else System.out.println("Searching canceled. Exiting...");
			eventer.event(DONE_LOADING, c);
		}catch(Exception e) {
			e.printStackTrace();
			eventer.error(s.juid(), Bus.ERROR_LOADING, e.toString());
		}
		eventer.end();
		System.gc(); //?
	}
	
	public static String loadJSON(String link) throws Exception {
		StringBuffer buffer = new StringBuffer();
		URLConnection con = new URL(link).openConnection();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
	        String line = "";
	        while ((line = reader.readLine()) != null) {
	            buffer.append(line).append("\n");
	        }
		}
		return buffer.toString();
	}
	public static void saveBytes(String link, String save, Reaction out) throws Exception {
        URLConnection con = new URL(link).openConnection();
        long allsize = con.getContentLength();
        try(BufferedInputStream in = new BufferedInputStream(con.getInputStream());
        		FileOutputStream fileOutputStream = new FileOutputStream(save)) {
            byte dataBuffer[] = new byte[1024];
            long cursize = 0;
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                if(out!=null) {
                    cursize+=bytesRead;
                    out.progress("progress", 1f*cursize/allsize);
                }
            }
        }
    }
	//Whats todo-Actions then starting AsyncJob? 
	@Override public void begin() {
		status.begin();
		main.begin();
		search.begin();
	}
	//Whats todo-Actions then ending AsyncJob? 
	@Override public void end() {
		status.end();
		search.end();
	}
	@Override public void windowClosing(WindowEvent arg0) {
		System.out.println("[Bus] Destroying Threads...");
		unlink();
		this.setVisible(false);
		this.dispose();
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		resume();
		System.out.println("[Bus] Wake uping Threads...");
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		pause();
		System.out.println("[Bus] Sleeping Threads...");
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		link(TMAN);
	}
	
	@Override public void windowDeactivated(WindowEvent arg0) {}
	@Override public void windowActivated(WindowEvent arg0) {}
	@Override public void windowClosed(WindowEvent arg0) {}

}
