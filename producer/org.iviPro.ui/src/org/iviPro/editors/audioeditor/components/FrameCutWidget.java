package org.iviPro.editors.audioeditor.components;

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
import org.iviPro.mediaaccess.player.AbstractMediaPlayer;
import org.iviPro.utils.SivaTime;

public class FrameCutWidget extends SivaComposite {

	private Text sFrame;
	private Text eFrame;

	private SivaTime startTime;
	private SivaTime endTime;

	private boolean startChanged;
	private final int START_TIME_CHANGED = 0;
	private final int END_TIME_CHANGED = 1;
	private AbstractMediaPlayer mp;
	private int maxFrame;

	public FrameCutWidget(Composite parent, int style, SivaTime startTime,
			SivaTime endTime, AbstractMediaPlayer mp) {
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
		sFrame.setData(new Integer(START_TIME_CHANGED));

		eFrame = new Text(this, SWT.BORDER);
		eFrame.setLayoutData(frameInputGD);
		eFrame.setText("" + this.endTime.getFrame()); //$NON-NLS-1$
		eFrame.setData(new Integer(END_TIME_CHANGED));
		addListener();
	}

	private void addListener() {
		KeyAdapter keyListener = new KeyAdapter() {

			public void keyPressed(KeyEvent e) {

				Text source = ((Text) e.getSource());

				// prüfe ob die Start oder Endzeit verändert wird
				Integer type = (Integer) ((Text) e.getSource()).getData();
				if (type.intValue() == START_TIME_CHANGED) {
					startChanged = true;
				} else if (type.intValue() == END_TIME_CHANGED) {
					startChanged = false;
				}

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

		// verifiziert die Eingabe in den Feldern
		VerifyListener verifyListener = new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent event) {
				event.doit = false;

				// erlaube eingetippte Zahlen
				if (Character.isDigit(event.character)) {
					event.doit = true;
				}
				// erlaube Backspace und Entf
				if (event.keyCode == 8 || event.keyCode == 127) {
					event.doit = true;
				}
				// erlaube beim Setzen des Inhalts per setText nur Zahlenstrings
				Pattern p = Pattern.compile("[-]?[0-9]+"); //$NON-NLS-1$
				Matcher m = p.matcher(event.text);
				if (m.matches()) {
					event.doit = true;
				}
			}
		};

		sFrame.addKeyListener(keyListener);
		sFrame.addVerifyListener(verifyListener);

		eFrame.addKeyListener(keyListener);
		eFrame.addVerifyListener(verifyListener);
	}

	private void setFrame(boolean notify) {
		SivaTime time;
		SivaEventType type;
		if (startChanged) {
			int frame = Integer.valueOf(sFrame.getText());
			if (frame >= endTime.getFrame()) {
				frame = endTime.getFrame() - 1;
			}
			time = new SivaTime(mp.getTimeForFrame(frame));
			type = SivaEventType.STARTTIME_CHANGED;
		} else {
			int frame = Integer.valueOf(eFrame.getText());
			if (frame < startTime.getFrame()) {
				frame = startTime.getFrame() + 1;
			}
			if (frame > maxFrame) {
				frame = maxFrame;
			}
			time = new SivaTime(mp.getTimeForFrame(frame));
			type = SivaEventType.ENDTIME_CHANGED;
		}

		if (notify) {
			SivaEvent event = new SivaEvent(null, type, time);
			notifySivaEventConsumers(event);
		}
	}

	public void setValue(SivaEvent event) {
		if (event.getEventType().equals(SivaEventType.STARTTIME_CHANGED)) {
			sFrame.setText("" + mp.getFrameForTime(event.getTime())); //$NON-NLS-1$
		} else if (event.getEventType().equals(SivaEventType.ENDTIME_CHANGED)) {
			eFrame.setText("" + mp.getFrameForTime(event.getTime())); //$NON-NLS-1$
		}
	}

}
