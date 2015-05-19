package org.iviPro.scenedetection.sd_main;

import java.util.GregorianCalendar;

public class SDTime {

	private int intHours = 0;

	private int intMinutes = 0;

	private int intSeconds = 0;

	private int intHSeconds = 0;

	private long nanoseconds = 0;

	private long milliSeconds = 0;

	private double seconds = 0;

	private double rest = 0;

	private GregorianCalendar cal;

	/**
	 * Erstellt aus einer Zeit in Nanosekunden ein Zeitobjekt mit den heraus-
	 * gerechneten Stunden, Minuten, Sekunden und Hundertstel-Sekunden.
	 * 
	 * @param nanoSekunden
	 *            Zeit in Nanosekunden (Milliardstel-Sekunden)
	 */
	public SDTime(long nanoSekunden) {

		cal = new GregorianCalendar();

		// Originalwert speichern
		nanoseconds = nanoSekunden;

		// Damit Einheiten gleich sind
		double nanosec = (double) nanoSekunden;

		// Einheiten, z.B. hat eine Minute 6*10^10 Nanosekunden...
		double std = 3.6E12;
		double min = 6E10;
		double sek = 1E9;
		double hdtsek = 1E7;
		double msec = 1E6;

		// Genauere Berechnung Gesamtzeit in Sekunden und Millisekunden
		seconds = nanoSekunden / sek;
		milliSeconds = (long) (nanoSekunden / msec);

		// Berechne aus den Nanosekunden Stunden, Minuten, ...
		intHours = (int) (nanosec / std);
		nanosec -= intHours * std;
		intMinutes = (int) (nanosec / min);
		nanosec -= intMinutes * min;
		intSeconds = (int) (nanosec / sek);
		nanosec -= intSeconds * sek;
		intHSeconds = (int) (nanosec / hdtsek);
		nanosec -= intHSeconds * hdtsek;

		// Speichere den Rest
		rest = nanosec;
	}

	public String getYear() {
		return String.valueOf(cal.get(GregorianCalendar.YEAR));
	}

	public String getMonth() {
		String newmonth;
		int month = cal.get(GregorianCalendar.MONTH) + 1;
		newmonth = month < 10 ? "0" + month : "" + month;
		return newmonth;
	}

	public String getDays() {
		String newdays;
		int days = cal.get(GregorianCalendar.DAY_OF_MONTH);
		newdays = days < 10 ? "0" + days : "" + days;
		return newdays;
	}

	/**
	 * @return Stunden
	 */
	public int getStunden() {
		return intHours;
	}

	/**
	 * @return Minuten
	 */
	public int getMinuten() {
		return intMinutes;
	}

	/**
	 * @return Sekunden
	 */
	public int getSekunden() {
		return intSeconds;
	}

	/**
	 * @return Hundertstel Sekunden
	 */
	public int getHundertstelSekunden() {
		return intHSeconds;
	}

	/**
	 * @return Gesamte Zeit in Tausendstel Sekunden
	 */
	public long getMilliSekunden() {
		return milliSeconds;
	}

	/**
	 * @return Gesamte Zeit in Nanosekunden
	 */
	public long getNanoseconds() {
		return nanoseconds;
	}

	/**
	 * @return Gesamte Zeit in Sekunden als Double
	 */
	public double getSeconds() {
		return seconds;
	}

	/**
	 * Liefert, falls benötigt, alles was unter Hundertstelsekunden liegt.
	 * 
	 * @return Rest in Nanosekunden
	 */
	public double getRest() {
		return rest;
	}
}
