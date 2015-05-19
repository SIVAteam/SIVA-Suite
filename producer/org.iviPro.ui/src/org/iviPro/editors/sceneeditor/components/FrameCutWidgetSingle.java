package org.iviPro.editors.sceneeditor.components;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.iviPro.editors.common.SivaComposite;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.mediaaccess.player.I_MediaPlayer;
import org.iviPro.model.resources.Video;
import org.iviPro.utils.SivaTime;

public class FrameCutWidgetSingle extends SivaComposite {

	private Text sFrame;

	private SivaTime startTime;

	private SivaTime endTime;

	private final int TIME_CHANGED = 1;

	private I_MediaPlayer mp;

	private int maxFrame;

	public FrameCutWidgetSingle(Composite parent, int style,
			SivaTime startTime, SivaTime endTime, I_MediaPlayer mp) {
		super(parent, style);

		this.startTime = new SivaTime(startTime.getNano());
		this.startTime.setFrame(startTime.getFrame());
		this.endTime = new SivaTime(endTime.getNano());
		this.endTime.setFrame(endTime.getFrame());
		this.mp = mp;

		// Maximalen Frame anhand der Videodauer berechnen
		this.maxFrame = mp.getFrameForTime(mp.getDuration());

		setLayout(new GridLayout(1, false));

		Label titel = new Label(this, SWT.None);
		titel.setText(Messages.FrameCutWidget_FrameLabelText);

		GridData frameInputGD = new GridData();
		frameInputGD.widthHint = 20;

		sFrame = new Text(this, SWT.BORDER);
		sFrame.setLayoutData(frameInputGD);
		sFrame.setText("" + this.startTime.getFrame()); //$NON-NLS-1$
		sFrame.setData(new Integer(TIME_CHANGED));

		addListener();
	}

	private void addListener() {
		KeyAdapter keyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				Text source = ((Text) e.getSource());

				String text = source.getText();
				long val = 0;
				try {
					val = Long.parseLong(text);
				} catch (Exception err) {
				}

				// Pfeiltaste nach oben
				if (e.keyCode == 16777217) {
					val = val + 1;
					text = "" + val; //$NON-NLS-1$					
					source.setText(text);
					setFrame(true);
				}

				// Pfeiltaste nach unten
				if (e.keyCode == 16777218) {
					val = val - 1;
					if (val < 0) {
						val = 0;
					}
					text = "" + val; //$NON-NLS-1$
					source.setText(text);
					setFrame(true);
				}
			}

			public void keyReleased(KeyEvent e) {
				setFrame(true);
			}
		};

		VerifyListener verifyListener = new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent event) {
				event.doit = false;

				if (Character.isDigit(event.character)) {
					event.doit = true;
				}
				if (event.keyCode == 8 || event.keyCode == 127) {
					event.doit = true;
				}
				Pattern p = Pattern.compile("[-]?[0-9]+");
				Matcher m = p.matcher(event.text);
				if (m.matches()) {
					event.doit = true;
				}
			}
		};

		sFrame.addKeyListener(keyListener);
		sFrame.addVerifyListener(verifyListener);
	}

	private void setFrame(boolean notify) {
		SivaTime time;
		SivaEventType type;
		int frame = Integer.valueOf(sFrame.getText());
		if (frame > maxFrame) {
			frame = maxFrame;
			SivaEvent ev = new SivaEvent(null, SivaEventType.ENDTIME_CHANGED, endTime);
			setValue(ev);
		}
		time = new SivaTime(mp.getTimeForFrame(frame));
		type = SivaEventType.ENDTIME_CHANGED;

		if (notify) {
			SivaEvent event = new SivaEvent(null, type, time);
			notifySivaEventConsumers(event);
		}
	}

	public void setValue(SivaEvent event) {
		long nano = event.getTime().getNano();
		Video vid = (Video) mp.getMediaObject();
		double frameRate = vid.getFrameRate();
		double seconds = (double) 1/frameRate;
		long time = (long) (seconds * 1000000000L);
		long newnano = nano % time;
		long frame = mp.getFrameForTime(event.getTime());
		if ((newnano + 1000000L) / time == 1) {
			frame++;
		}
		sFrame.setText("" + frame);
	}
	
	public void setStartFrame() {
		sFrame.setText("0");
		setFrame(true);
	}

	public int getFrame() {
		return Integer.valueOf(sFrame.getText());
	}
}
