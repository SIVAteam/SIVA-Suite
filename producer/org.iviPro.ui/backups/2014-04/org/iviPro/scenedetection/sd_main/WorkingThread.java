package org.iviPro.scenedetection.sd_main;

import java.util.Observable;
import java.util.Observer;
import org.eclipse.core.runtime.IProgressMonitor;

public class WorkingThread extends Thread implements Observer {

	public static final int updateTime = 1000;
	
	private static int workedSinceLastTime = 0;
	
	private IProgressMonitor monitor;

	private boolean working;
	
	private String function;
	
	public WorkingThread(IProgressMonitor monitor, String function) {
		this.function = function;
		this.working = true;
		this.monitor = monitor;
	}

	public void run() {
		while (working) {
			try {
				monitor.worked(updateWorkAmount());
				sleep(updateTime);
			} catch (InterruptedException e) {
				System.err.println("Workingthread was interrupted.");
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		this.working = false;		
	}
	
	public int updateWorkAmount() {
		int currentworked = ProgressDetermination.getWorked(function);
		int result = currentworked - workedSinceLastTime;
		workedSinceLastTime = currentworked;
		return result;
	}
}
