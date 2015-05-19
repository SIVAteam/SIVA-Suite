package org.iviPro.utils;

import java.text.DecimalFormat;

/**
 * Klasse für Zeitoperationen Bei einem ITime Objekt können Listener registriert
 * werden, die auf Änderungen reagieren
 */
public class SivaTime implements Comparable {

	// die ursprüngliche Zeit in Nanosekunden
	private long nano = 0;
	// Korrespondierende Framenummer
	private int frame = 0;

	// konvertierte Werte, diese werden aus
	// der ursprünglichen Zeit berechnet
	private long con_hours = 0;
	private long con_minutes = 0;
	private long con_seconds = 0;
	private long con_milliseconds = 0;
	private long con_nanoseconds = 0;

	public SivaTime(long nano) {
		if (nano < 0) {
			throw new IllegalArgumentException("Time value may not be negative."); //$NON-NLS-1$
		}
		this.nano = nano;
		convertFromNano();
	}
	
	public SivaTime(SivaTime newTime) {
		this.con_hours = newTime.getCon_hours();
		this.con_milliseconds = newTime.getCon_milliseconds();
		this.con_minutes = newTime.getCon_minutes();
		this.con_nanoseconds = newTime.getCon_nanoseconds();
		this.con_seconds = newTime.getCon_milliseconds();
		this.frame = newTime.getFrame();
		this.nano = newTime.getNano();
	}

	public void setTime(long nano) {
		if (nano < 0) {
			throw new IllegalArgumentException("Time value may not be negative."); //$NON-NLS-1$
		}
		this.nano = nano;
		convertFromNano();
	}

	/**
	 * Interprets the given strings as long values and sets the time accordingly.
	 * <p>
	 * <b>Note:</b> 
	 * Values which can not be parsed to long are automatically set to 0.
	 * 
	 * @param hours string representing hours
	 * @param minutes string representing minutes
	 * @param seconds string representing seconds
	 * @param milliseconds string representing milliseconds
	 * @param nanoseconds string representing nanoseconds
	 */
	public void setTime(String hours, String minutes, String seconds,
			String milliseconds, String nanoseconds) {
		setTime(getLongValue(hours), getLongValue(minutes),
				getLongValue(seconds), getLongValue(milliseconds),
				getLongValue(nanoseconds));
	}
	
	/**
	 * Interprets the given strings as long values and sets the time accordingly.
	 * Additionally, stores a frame number corresponding with this time.
	 * <p>
	 * <b>Note:</b> 
	 * Values which can not be parsed to long are automatically set to 0.
	 * 
	 * @param hours string representing hours
	 * @param minutes string representing minutes
	 * @param seconds string representing seconds
	 * @param milliseconds string representing milliseconds
	 * @param nanoseconds string representing nanoseconds
	 * @param frame frame number corresponding to the time value
	 */
	public void setTime(String hours, String minutes, String seconds,
			String milliseconds, String nanoseconds, String frame) {
		setTime(getLongValue(hours), getLongValue(minutes),
				getLongValue(seconds), getLongValue(milliseconds),
				getLongValue(nanoseconds), getLongValue(frame));
	}

	private long getLongValue(String str) {
		try {
			Long.parseLong(str);
		} catch (Exception e) {
			return 0L;
		}
		return Long.parseLong(str);
	}

	/**
	 * setzt die Zeit richtig d.h. werden z.B. 80 Minuten übergeben wird das
	 * umgewandelt zu 1 Stunde und 20 Minuten
	 * 
	 * @param hours
	 * @param minutes
	 * @param seconds
	 * @param milliseconds
	 * @param nanoseconds
	 */
	private void setTime(long hours, long minutes, long seconds,
			long milliseconds, long nanoseconds) {
		// temporäre Variable zum Zwischenspeichern
		long temp = 0;

		// Nanosekunden im Millisekundenbereich
		// aktuell werden die Nanosekunden nicht weiter beachtet, der Wert ist
		// auf 0 gesetzt -> wegen Rundungsfehlern
		if (nanoseconds > 999999L) {
			temp = nanoseconds / 1000000L;
			milliseconds = milliseconds + temp;
			nanoseconds = nanoseconds - temp * 1000000L;
		}

		if (milliseconds < 0) {
			// Millisekunden können nur nach unten geändert werden
			// wenn Stunden, Minuten oder Sekuden vorhanden sind
			if (hours > 0 || minutes > 0 || seconds > 0) {
				milliseconds = 1000L + milliseconds;
				seconds--;
			}
		}

		// Millisekunden im Sekundenbereich
		if (milliseconds > 999) {
			temp = milliseconds / 1000L;
			seconds = seconds + temp;
			milliseconds = milliseconds - temp * 1000L;
		}

		if (seconds < 0) {
			// Sekunden können nur nach unten geändert werden
			// wenn Stunden und Minuten vorhanden sind
			if (hours > 0 || minutes > 0) {
				seconds = 60L + seconds;
				if (minutes - 1 >= 0) {
					minutes--;
				}
			}
		}

		// Sekunden im Minutenbereich
		if (seconds > 59) {
			temp = seconds / 60L;
			minutes = minutes + temp;
			seconds = seconds - temp * 60L;
		}

		if (minutes < 0) {
			if (hours > 0) {
				minutes = 60L + minutes;
				hours--;
			}
		}

		// Minuten im Stundenbereich
		if (minutes > 59) {
			temp = minutes / 60L;
			hours = hours + temp;
			minutes = minutes - temp * 60L;
		}

		if (hours < 0) {
			hours = 0;
		}

		this.con_hours = hours;
		this.con_minutes = minutes;
		this.con_seconds = seconds;
		this.con_milliseconds = milliseconds;
		this.con_nanoseconds = nanoseconds;
		convertToNano();
	}
	
	private void setTime(long hours, long minutes, long seconds,
			long milliseconds, long nanoseconds, long frame) {
		this.frame = (int)frame;
		setTime(hours, minutes, seconds, milliseconds, nanoseconds);
	}

	/**
	 * zieht die übergebene Zeit von dieser Zeit ab und gibt das Ergebniss als
	 * long zurück (diese Zeit wird aber nicht neu gesetzt!)
	 * 
	 * @param subTime
	 * @return
	 */
	public long subTime(SivaTime subTime) {
		return this.getNano() - subTime.getNano();
	}
	
	/**
	 * zieht die übergebene Zeit von dieser Zeit ab und gibt das Ergebniss als
	 * SivaTime zurück (diese Zeit wird aber nicht neu gesetzt!)
	 * 
	 * @param subTime
	 * @return
	 */
	public SivaTime subTimeS(SivaTime subTime) {
		return new SivaTime(this.getNano() - subTime.getNano());
	}

	/**
	 * addiert die übergebene Zeit zu dieser Zeit und gibt das Ergebniss als
	 * long zurück (diese Zeit wird aber nicht neu gesetzt!)
	 * 
	 * @param addTime
	 * @return
	 */
	public long addTime(SivaTime addTime) {
		return this.getNano() + addTime.getNano();
	}
	
	/**
	 * addiert die übergebene Zeit zu dieser Zeit und gibt das Ergebniss als
	 * SivaTime zurück (diese Zeit wird aber nicht neu gesetzt!)
	 * 
	 * @param addTime	 
	 * @return
	 */
	public SivaTime addTimeS(SivaTime addTime) {
		return new SivaTime(this.getNano() + addTime.getNano());
	}

	/**
	 * addiert die übergebene Zeit zu dieser Zeit und gibt das Ergebniss als
	 * SivaTime zurück (diese Zeit wird aber nicht neu gesetzt!)
	 * 
	 * @param addTime	 
	 * @return
	 */
	public SivaTime addTimeS(long addTime) {
		return new SivaTime(this.getNano() + addTime);
	}
	
	/**
	 * splittet den ursprünglichen Nanosekundenwert in Stunden, Minuten,
	 * Sekunden und Nanosekunden auf
	 */
	private void convertFromNano() {
		con_milliseconds = nano / 1000000L;
		con_nanoseconds = nano - con_milliseconds * 1000000L;
		con_seconds = con_milliseconds / 1000L;
		con_milliseconds = con_milliseconds - con_seconds * 1000L;
		con_minutes = con_seconds / 60L;
		con_seconds = con_seconds - con_minutes * 60L;
		con_hours = con_minutes / 60L;
		con_minutes = con_minutes - con_hours * 60L;
	}

	/**
	 * konvertiert die aufgespliteten Werte in Nanosekunden
	 */
	private void convertToNano() {
		nano = nanoFromSeconds(con_seconds) + nanoFromMillis(con_milliseconds)
				+ nanoFromHours(con_hours) + nanoFromMinutes(con_minutes)
				+ con_nanoseconds;
	}

	/**
	 * Sekunden zu Nanosekunden
	 * 
	 * @param seconds
	 * @return
	 */
	private long nanoFromSeconds(long seconds) {
		return seconds * 1000000000L;
	}

	/**
	 * Millisekunden zu Nanosekunden
	 * 
	 * @param millis
	 * @return
	 */
	private long nanoFromMillis(long millis) {
		return millis * 1000000L;
	}

	/**
	 * Stunden zu Nanosekunden
	 * 
	 * @param hours
	 * @return
	 */
	private long nanoFromHours(long hours) {
		return hours * 3600L * 1000000000L;
	}

	/**
	 * Minuten zu Nanosekunden
	 * 
	 * @param minutes
	 * @return
	 */
	private long nanoFromMinutes(long minutes) {
		return minutes * 60L * 1000000000L;
	}

	public String toString() {
		return getTimeString(nano);
	}

	// not used at the moment
	/**
	 * passt die Zeit an die Dauer des Mediaplayers an. Falls die Zeit zu hoch
	 * ist wird auf das Maximum gesetzt
	 * 
	 * @param newTime
	 * @param duration
	 * @return
	 */
	/*public void adjustTimeToPlayer(long duration) {
		if (nano < 0) {
			nano = 0;
			convertFromNano();
		}
		if (nano > duration) {
			nano = duration;
			convertFromNano();
		}
	}*/

	public long getNano() {
		return nano;
	}
	
	public long getMillis() {
		return this.getNano()/1000000L;
	}

	public long getCon_hours() {
		return con_hours;
	}

	public long getCon_minutes() {
		return con_minutes;
	}

	public long getCon_seconds() {
		return con_seconds;
	}

	public long getCon_milliseconds() {
		return con_milliseconds;
	}

	public long getCon_nanoseconds() {
		return con_nanoseconds;
	}

	/**
	 * liefert einen String der Form Stunden:Minuten:Sekunden zurück
	 * 
	 * @param die
	 *            zu formatierende Zeit in Nanosekunden
	 */
	public static String getTimeString(long nanosec) {
		long seconds = nanosec / 1000000000L;
		long minutes = seconds / 60L;
		seconds = seconds - minutes * 60L;
		long hour = minutes / 60L;
		minutes = minutes - hour * 60L;
		DecimalFormat format = new java.text.DecimalFormat("00"); //$NON-NLS-1$
		return format.format(hour) + ":" + format.format(minutes) + ":" //$NON-NLS-1$ //$NON-NLS-2$
				+ format.format(seconds);
	}

	/**
	 * liefert einen String der Form Stunden:Minuten:Sekunden.Millisekunden
	 * zurück
	 * 
	 * @param die
	 *            zu formatierende Zeit in Nanosekunden
	 */
	public static String getTimeStringLong(long nanosec) {
		long milliSeconds = nanosec / 1000000L;
		long seconds = milliSeconds / 1000L;
		milliSeconds = milliSeconds - seconds * 1000L;
		long minutes = seconds / 60L;
		seconds = seconds - minutes * 60L;
		long hour = minutes / 60L;
		minutes = minutes - hour * 60L;
		DecimalFormat format = new java.text.DecimalFormat("00"); //$NON-NLS-1$
		DecimalFormat formatMilli = new java.text.DecimalFormat("000"); //$NON-NLS-1$
		return format.format(hour) + ":" + format.format(minutes) + ":" //$NON-NLS-1$ //$NON-NLS-2$
				+ format.format(seconds) + "." //$NON-NLS-1$
				+ formatMilli.format(milliSeconds);
	}

	/**
	 * Gibt eine String-Repraesentation dieses Zeit-Objekts zurueck, die konform
	 * zur Zeit-Darstellung im SIVA XML-Format ist.
	 * 
	 * @param nanosec
	 * @return
	 */
	public static String getSivaXMLTime(long nanosec) {
		String timeStr = getTimeStringLong(nanosec);
		return timeStr;
	}

	@Override
	public int compareTo(Object compTime) {
		if (compTime == null) {
			throw new NullPointerException();
		}
		SivaTime compareTime = (SivaTime) compTime;
		if (this.nano - compareTime.getNano() < 0) {
			return -1;
		} else if (this.nano - compareTime.getNano() > 0) {
			return 1;
		}
		return 0;
	}
	
	/**
	 * Setzt die Framenummer
	 * @param frame Framenummer
	 */
	public void setFrame(int frame) {
		this.frame = frame;
	}
	
	/**
	 * Gibt die Framenummer zurück
	 * @return	Framenummer
	 */
	public int getFrame() {
		return this.frame;
	}
}
