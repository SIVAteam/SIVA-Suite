package org.iviPro.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventProvider;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.mediaaccess.player.MediaPlayer;
import org.iviPro.mediaaccess.player.MediaPlayerWidget;
import org.iviPro.mediaaccess.player.PlayerFactory;
import org.iviPro.model.resources.Video;

/**
 * Time selection element for videos opening a new shell on the current Display 
 * containing a media player and a selection button. These can be used to choose
 * a specific time in the video. On selection, listening 
 * {@link SivaEventConsumerI SivaEventConsumers} are informed about the choice. 
 * @author John
 *
 */
public class TimeSelector extends SivaEventProvider {
	
	Shell shell;
	Video thumbVideo;
	long thumbTime = 0;
	
	/**
	 * Constructs a selector with the given parameters. 
	 * @param video video on which selection is done
	 * @param width width of the shell
	 * @param height height of the shell
	 * @param startTime start time at which the media player begins
	 * @param endTime end time at which the media player stops
	 */
	public TimeSelector(Video video, int width, int height, long startTime, long endTime) {
		thumbVideo = video;
		shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM 
				| SWT.APPLICATION_MODAL);
		shell.setLayout(new GridLayout(1, false));
			
		final MediaPlayer mp = PlayerFactory.getPlayer(video, startTime, endTime);		
		MediaPlayerWidget mpWidget = new MediaPlayerWidget(shell, SWT.NONE, mp, height, true, false);

		// Select button
		Button selectButton = new Button(shell, SWT.PUSH);
		selectButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		selectButton.setText("Select current image");
		selectButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				SivaEvent event = new SivaEvent(TimeSelector.this,
						SivaEventType.TIME_SELECTION, mp.getMediaTime().getNano());
				notifySivaEventConsumers(event);
				shell.close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);				
			}
		});
		
		shell.pack();
		shell.open();
	}
}
