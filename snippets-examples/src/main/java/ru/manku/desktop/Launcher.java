package ru.manku.desktop;

import java.io.File;

import javax.swing.SwingUtilities;

import snippets.threads.Job;
import snippets.threads.Threads;
import snippets.threads.defaults.DefaultManager;

public class Launcher {
	public static final String ROOT = System.getProperty("user.home").concat("/Coub Alter Player/");
	public static void main(String[] args) {
		new File(ROOT.concat(".cache/")).mkdirs();
		new File(ROOT.concat("exported/")).mkdirs();
		
		for(File f: new File(ROOT.concat(".cache/")).listFiles()) f.delete();
		
		final SwingJob runUI = new SwingJob() {
			@Override public Job register(Job job) {
				//Please, prepare Snippet IORC-state by calling match(...) before raw calling!
				job.function.match(job);
				SwingUtilities.invokeLater(job.function); 
				return job;
			}
		};
		final DefaultManager defman = new DefaultManager(runUI);
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				new Bus(defman);
			}});
	}

}
