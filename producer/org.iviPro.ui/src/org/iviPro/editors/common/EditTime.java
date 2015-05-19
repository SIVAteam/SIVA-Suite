package org.iviPro.editors.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.utils.NumericInputListener;
import org.iviPro.utils.SivaTime;

public class EditTime extends SivaComposite {

	// die Start und die Endzeit
	private SivaTime sTime;
	private SivaTime eTime;
		
	// Felder für die Startzeit
	private Text sHours;
	private Text sMinutes;
	private Text sSeconds;
	private Text sMillis;
	
	// Felder für die Endzeit
	private Text eHours;
	private Text eMinutes;
	private Text eSeconds;
	private Text eMillis;
	
	// wird verwendet um festzustellen ob die Start oder Endzeit verändert wurde
	private final int START_TIME_CHANGED = 0;
	private final int END_TIME_CHANGED = 1;
	
	// gibt an ob die Start- oder Endzeit verändert wurde
	private boolean startChanged = false;	
	
	// der maximale Wert
	private long maxVal;

	/**
	 * Component for editing the given start and end times in the format 
	 * hh::mm::ss while respecting a given maximum value. Changes are stored
	 * directly in the given time objects. 
	 * @param parent parent component
	 * @param style SWT style to be used
	 * @param startTime object holding the start time
	 * @param endTime object holding the end time
	 * @param maxVal allowed maximum time in nanoseconds
	 */
	public EditTime(Composite parent, int style, SivaTime startTime, SivaTime endTime, long maxVal) {
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
		doublePointGD.widthHint = 2;

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

		final Label labStartTime = new Label(this, SWT.LEFT);
		labStartTime.setText(Messages.EditTime_Label_StartTime);
		labStartTime.setLayoutData(new GridData(47,14)); //wegen duration label-Breite notwendig
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

		final Label labEndTime = new Label(this, SWT.LEFT);
		labEndTime.setText(Messages.EditTime_Label_EndTime);
		labEndTime.setLayoutData(new GridData(47,14));
		eHours = new Text(this, SWT.CENTER | SWT.BORDER);
		eHours.setLayoutData(timeInputFieldsGD);
		eHours.setData(new Integer(this.END_TIME_CHANGED));
		eHours.setText("" + eTime.getCon_hours()); //$NON-NLS-1$
		doublePoint = new Label(this, SWT.CENTER);
		doublePoint.setText(":"); //$NON-NLS-1$
		doublePoint.setLayoutData(doublePointGD);
		eMinutes = new Text(this, SWT.CENTER | SWT.BORDER);
		eMinutes.setLayoutData(timeInputFieldsGD);
		eMinutes.setData(new Integer(this.END_TIME_CHANGED));
		eMinutes.setText("" + eTime.getCon_minutes()); //$NON-NLS-1$
		doublePoint = new Label(this, SWT.CENTER);
		doublePoint.setText(":"); //$NON-NLS-1$
		doublePoint.setLayoutData(doublePointGD);
		eSeconds = new Text(this, SWT.CENTER | SWT.BORDER);
		eSeconds.setLayoutData(timeInputFieldsGD);
		eSeconds.setData(new Integer(this.END_TIME_CHANGED));
		eSeconds.setText("" + eTime.getCon_seconds()); //$NON-NLS-1$
		doublePoint = new Label(this, SWT.CENTER);
		doublePoint.setText("."); //$NON-NLS-1$
		doublePoint.setLayoutData(doublePointGD);
		eMillis = new Text(this, SWT.CENTER | SWT.BORDER);
		eMillis.setLayoutData(timeInputFieldsGD);
		eMillis.setData(new Integer(this.END_TIME_CHANGED));
		eMillis.setText("" + eTime.getCon_milliseconds()); //$NON-NLS-1$

		// setze Felder die aufgrund der Szenen/Annotationslänge nicht geändert werden
		// können auf nicht editierbar
		// die Endzeit der Szene
		if (eTime.getCon_hours() <= 0) {
			sHours.setEditable(false);
			eHours.setEditable(false);
			if (eTime.getCon_minutes() <= 0) {
				sMinutes.setEditable(false);
				eMinutes.setEditable(false);
				if (eTime.getCon_seconds() < 0) {
					sSeconds.setEditable(false);
					eSeconds.setEditable(false);
				}
			}
		}
		
		// Keyadapter für die Eingabefelder
		KeyAdapter keyListener = new KeyAdapter() {
	
			public void keyPressed(KeyEvent e) {	
				
				Text source = ((Text) e.getSource());				
				
				// prüfe ob die Start oder Endzeit verändert wird
				Integer type = (Integer) ((Text) e.getSource()).getData();
				if (type.intValue() == START_TIME_CHANGED) {
					startChanged = true;
				} else 
				if (type.intValue() == END_TIME_CHANGED) {
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
					setTime(true);	
				}

				// Pfeiltaste nach unten
				if (e.keyCode == 16777218) {
					val = val - 1;
					if (val < 0 ) {
						val = 0;
					}
					text = "" + val; //$NON-NLS-1$
					source.setText(text);
					setTime(true);	
				}	
			}	
			
		};		
		
		//listener der beim verlassen der TExtfelder die Eingabe auf Gültigkeit prüft
		FocusAdapter focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event){
				setTime(true);
			}
		};
		
		// verifiziert die Eingabe in den Feldern
		VerifyListener numericinputListener = new NumericInputListener();			    				
		
		// KeyListener + VerfifyListener + Focuslistener hinzufügen
		sHours.addVerifyListener(numericinputListener);	
		sHours.addKeyListener(keyListener);	
		sHours.addFocusListener(focusListener);
		sMinutes.addVerifyListener(numericinputListener);	
		sMinutes.addKeyListener(keyListener);
		sMinutes.addFocusListener(focusListener);
		sSeconds.addVerifyListener(numericinputListener);	
		sSeconds.addKeyListener(keyListener);	
		sSeconds.addFocusListener(focusListener);
		sMillis.addVerifyListener(numericinputListener);	
		sMillis.addKeyListener(keyListener);
		sMillis.addFocusListener(focusListener);
		eHours.addVerifyListener(numericinputListener);
		eHours.addKeyListener(keyListener);	
		eHours.addFocusListener(focusListener);
		eMinutes.addVerifyListener(numericinputListener);	
		eMinutes.addKeyListener(keyListener);
		eMinutes.addFocusListener(focusListener);
		eSeconds.addVerifyListener(numericinputListener);	
		eSeconds.addKeyListener(keyListener);
		eSeconds.addFocusListener(focusListener);
		eMillis.addVerifyListener(numericinputListener);	
		eMillis.addKeyListener(keyListener);
		eMillis.addFocusListener(focusListener);
		
	}
	
	public void setValue(SivaEvent event) {	
		SivaTime value = new SivaTime(event.getTime().getNano());
		SivaEventType type = event.getEventType();
		if (type.equals(SivaEventType.STARTTIME_CHANGED)) {
			startChanged = true;
			sTime = value;
			setStartTime(sTime);
		} else 
		if (type.equals(SivaEventType.ENDTIME_CHANGED)) {
			startChanged = false;
			eTime = value;
			setEndTime(eTime);
		};
		setTime(false);
	}
	
	/*
	 * setzt die Start- und Endzeit entsprechend den veränderten Werten
	 * falls die Zeit innerhalb EditTime geändert wird, muss der MediaPlayer gesetzt werden
	 * wird die Zeit extern gesetzt ist keine Benachrichtigung des Players erforderlich
	 */
	private void setTime(boolean notify) {
		SivaTime time;
		SivaEventType type;
		SivaTime newTime = new SivaTime(0);
		if (startChanged) {
			newTime.setTime(sHours.getText(), sMinutes.getText(), sSeconds.getText(), sMillis.getText(), "0"); //$NON-NLS-1$
			if (newTime.getNano() >= eTime.getNano()) {
				sTime.setTime(eTime.getNano() - 1000000L);
			} else {
				sTime.setTime(sHours.getText(), sMinutes.getText(), sSeconds.getText(), sMillis.getText(), "0"); //$NON-NLS-1$
			}
			time = sTime;
			type = SivaEventType.STARTTIME_CHANGED;
			setStartTime(sTime);
		} else {
			newTime.setTime(eHours.getText(), eMinutes.getText(),	eSeconds.getText(), eMillis.getText(), "0"); //$NON-NLS-1$
			
			if (newTime.getNano() <= sTime.getNano()) {
				eTime.setTime(sTime.getNano() + 1000000L);
			} else
			if (newTime.getNano() > maxVal) {
				eTime.setTime(maxVal);
			} else {
				eTime.setTime(eHours.getText(), eMinutes.getText(),	eSeconds.getText(), eMillis.getText(), "0"); //$NON-NLS-1$
			}
			time = eTime;
			type = SivaEventType.ENDTIME_CHANGED;
			setEndTime(eTime);
		}
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
	
	private void setEndTime(SivaTime time) {
		eHours.setText("" + eTime.getCon_hours()); //$NON-NLS-1$
		eHours.setSelection(eHours.getCharCount());
		eMinutes.setText("" + eTime.getCon_minutes()); //$NON-NLS-1$
		eMinutes.setSelection(eMinutes.getCharCount());
		eSeconds.setText("" + eTime.getCon_seconds()); //$NON-NLS-1$
		eSeconds.setSelection(eSeconds.getCharCount());
		eMillis.setText("" + eTime.getCon_milliseconds());		 //$NON-NLS-1$
		eMillis.setSelection(eMillis.getCharCount());	
	}
}