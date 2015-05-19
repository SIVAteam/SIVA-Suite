package org.iviPro.editors.events;

import org.iviPro.utils.SivaTime;

public class SivaEvent {

	// wird normalerweise null gesetzt, es kann vorkommen, dass Meldungen die von der 
	// Quelle erzeugt werden wieder an die Quelle gehen => Source wird auf die Quelle gesetzt
	// z.B. der Video Slider setzt die MediaTime und der MediaPlayer benachrichtigt wieder den Slider ..
	// obwohl der Slider bereits auf dem aktuellen Wert steht.
	// z.B. in AnnotationDefineWidget
	private SivaEventProviderI source;
	private SivaEventType type;
	private Object value;
	private SivaTime time;
	
	public SivaEvent(SivaEventProviderI source) {
		this.source = source;
		this.type = SivaEventType.NO_TYPE;
		this.value = null;
	}
		
	public SivaEvent(SivaEventProviderI source, Object value) {
		this.source = source;
		this.type = SivaEventType.NO_TYPE;
		this.value = value;
	}
	
	public SivaEvent(SivaEventProviderI source, SivaEventType type, Object value) {
		this.source = source;
		this.type = type;
		this.value = value;
	}
	
	public SivaEvent(SivaEventProviderI source, SivaEventType type, SivaTime time) {
		this.source = source;
		this.type = type;
		this.time = time;
	}
	
	public SivaEventType getEventType() {
		return this.type;
	}
	
	public Object getValue() {
		return this.value;
	}
	
	public SivaTime getTime() {
		return this.time;
	}
	
	public SivaEventProviderI getSource() {
		return this.source;
	}
	
	public String toString() {
		return "SivaEvent(Source:" + source + " Type: " + type + " value: " + value + " time: " + time; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
