package org.iviPro.editors.common;

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
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.utils.SivaTime;

public class EditTimeSingle extends SivaComposite {

	private SivaTime sTime;

	private SivaTime eTime;

	private Text sHours;

	private Text sMinutes;

	private Text sSeconds;

	private Text sMillis;

	private long maxVal;

	private final int START_TIME_CHANGED = 0;

	public EditTimeSingle(Composite parent, int style, SivaTime startTime,
			SivaTime endTime, long maxVal) {
		super(parent, style);
		this.maxVal = maxVal;
		this.sTime = new SivaTime(startTime.getNano());
		this.eTime = new SivaTime(endTime.getNano());
		createTimeEditComp(parent);
	}

	private void createTimeEditComp(Composite parent) {
		// Composite für das Einstellen der Zeit
		GridLayout timeGL = new GridLayout(8, false);
		timeGL.marginWidth = 0;
		setLayout(timeGL);

		GridData timeInputFieldsGD = new GridData();
		timeInputFieldsGD.widthHint = 20;
		timeInputFieldsGD.heightHint = 14;

		GridData doublePointGD = new GridData();
		doublePointGD.widthHint = 5;

		// Label für die Start und Endzeit
		// jedes Eingabefeld erhält einen Identifier mit
		// welcher im Data Field des Widgets gespeichert wird
		final Label labTimeHead = new Label(this, SWT.CENTER);
		labTimeHead.setText("          "); //$NON-NLS-1$
		final Label headHour = new Label(this, SWT.CENTER);
		headHour.setText("    h"); //$NON-NLS-1$
		Label doublePoint = new Label(this, SWT.CENTER);
		doublePoint.setText(" "); //$NON-NLS-1$
		final Label headMinutes = new Label(this, SWT.CENTER);
		headMinutes.setText("   m"); //$NON-NLS-1$
		doublePoint = new Label(this, SWT.CENTER);
		doublePoint.setText(" "); //$NON-NLS-1$
		final Label headSeconds = new Label(this, SWT.CENTER);
		headSeconds.setText("    s"); //$NON-NLS-1$
		doublePoint = new Label(this, SWT.CENTER);
		doublePoint.setText(" "); //$NON-NLS-1$
		final Label headMillis = new Label(this, SWT.CENTER);
		headMillis.setText("   ms"); //$NON-NLS-1$

		final Label labStartTime = new Label(this, SWT.CENTER);
		labStartTime.setText(Messages.EditTime_Label_EndTime);
		sHours = new Text(this, SWT.CENTER | SWT.BORDER);
		sHours.setLayoutData(timeInputFieldsGD);
		sHours.setText("" + sTime.getCon_hours()); //$NON-NLS-1$
		sHours.setData(new Integer(this.START_TIME_CHANGED));
		doublePoint = new Label(this, SWT.CENTER);
		doublePoint.setText(":"); //$NON-NLS-1$
		doublePoint.setLayoutData(doublePointGD);
		sMinutes = new Text(this, SWT.CENTER | SWT.BORDER);
		sMinutes.setLayoutData(timeInputFieldsGD);
		sMinutes.setText("" + sTime.getCon_minutes()); //$NON-NLS-1$
		sMinutes.setData(new Integer(this.START_TIME_CHANGED));
		doublePoint = new Label(this, SWT.CENTER);
		doublePoint.setText(":"); //$NON-NLS-1$
		doublePoint.setLayoutData(doublePointGD);
		sSeconds = new Text(this, SWT.CENTER | SWT.BORDER);
		sSeconds.setLayoutData(timeInputFieldsGD);
		sSeconds.setText("" + sTime.getCon_seconds()); //$NON-NLS-1$
		sSeconds.setData(new Integer(this.START_TIME_CHANGED));
		doublePoint = new Label(this, SWT.CENTER);
		doublePoint.setText("."); //$NON-NLS-1$
		doublePoint.setLayoutData(doublePointGD);
		sMillis = new Text(this, SWT.CENTER | SWT.BORDER);
		sMillis.setLayoutData(timeInputFieldsGD);
		sMillis.setText("" + sTime.getCon_milliseconds()); //$NON-NLS-1$
		sMillis.setData(new Integer(this.START_TIME_CHANGED));

		if (eTime.getCon_hours() <= 0) {
			sHours.setEditable(false);
			if (eTime.getCon_minutes() <= 0) {
				sMinutes.setEditable(false);
				if (eTime.getCon_seconds() < 0) {
					sSeconds.setEditable(false);
				}
			}
		}

		// Keyadapter für die Eingabefelder
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
					setTime(true);
				}

				// Pfeiltaste nach unten
				if (e.keyCode == 16777218) {
					val = val - 1;
					if (val < 0) {
						val = 0;
					}
					text = "" + val; //$NON-NLS-1$
					source.setText(text);
					setTime(true);
				}
			}

			public void keyReleased(KeyEvent e) {
				setTime(true);
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

		// KeyListener + VerfifyListener hinzufügen
		sHours.addVerifyListener(verifyListener);
		sHours.addKeyListener(keyListener);
		sMinutes.addVerifyListener(verifyListener);
		sMinutes.addKeyListener(keyListener);
		sSeconds.addVerifyListener(verifyListener);
		sSeconds.addKeyListener(keyListener);
		sMillis.addVerifyListener(verifyListener);
		sMillis.addKeyListener(keyListener);
	}

	public void setValue(SivaEvent event) {
		SivaTime value = new SivaTime(event.getTime().getNano());
		sTime = value;
		setStartTime(sTime);
		setTime(false);
	}

	private void setTime(boolean notify) {
		SivaTime time;
		SivaEventType type;
		SivaTime newTime = new SivaTime(0);

		newTime.setTime(sHours.getText(), sMinutes.getText(),
				sSeconds.getText(), sMillis.getText(), "0"); //$NON-NLS-1$
		System.out.println("MaxVal: "+maxVal + "NewTime: "+newTime.getNano() + "EndTime: "+eTime.getNano());
		if (newTime.getNano() > eTime.getNano()) {
			sTime.setTime(eTime.getNano() - 1000000L);
		} else {
			if (newTime.getNano() > maxVal) {
				sTime.setTime(maxVal);
			} else {
				sTime.setTime(sHours.getText(), sMinutes.getText(),
						sSeconds.getText(), sMillis.getText(), "0"); //$NON-NLS-1$
			}
		}
		time = sTime;
		type = SivaEventType.STARTTIME_CHANGED;
		setStartTime(sTime);
		if (notify) {
			SivaEvent event = new SivaEvent(null, type, time);
			notifySivaEventConsumers(event);
		}
	}

	private void setStartTime(SivaTime time) {
		sHours.setText("" + sTime.getCon_hours()); //$NON-NLS-1$
		sHours.setSelection(sHours.getCharCount());
		sMinutes.setText("" + sTime.getCon_minutes()); //$NON-NLS-1$
		sMinutes.setSelection(sMinutes.getCharCount());
		sSeconds.setText("" + sTime.getCon_seconds()); //$NON-NLS-1$
		sSeconds.setSelection(sSeconds.getCharCount());
		sMillis.setText("" + sTime.getCon_milliseconds()); //$NON-NLS-1$
		sMillis.setSelection(sMillis.getCharCount());
	}
	
	public SivaTime getTime() {
		return sTime;
	}
}
