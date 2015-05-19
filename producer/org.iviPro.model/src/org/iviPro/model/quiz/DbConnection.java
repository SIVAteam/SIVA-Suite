package org.iviPro.model.quiz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Klasse zum Aufbauen und Verwalten einer Datenbank-Verbindung.
 * 
 * @author Sabine Gattermann
 * 
 */
public class DbConnection {

    // Verbindungsdaten
    private String driver;
    private String url;
    private String user;
    private String password;

    // Die Verbindung
    private Connection con;
    // Der Verbindungsstatus
    private boolean connectionStatus = false;

    /**
     * Konstruktor
     * 
     * @param driver
     *            Der Datenbank-Treiber.
     * @param url
     *            Die Adresse.
     * @param user
     *            Der Benutzername.
     * @param password
     *            Das Passwort.
     */
    public DbConnection(String driver, String url, String user, String password) {
	this.driver = driver;
	this.url = url;
	this.user = user;
	this.password = password;
	this.connectionStatus = openDB();
    }

    /**
     * Liefert den Verbindungsstatus.
     * 
     * @return true, falls Verbindung besteht, false sonst.
     */
    public boolean getConnectionStatus() {
	return connectionStatus;
    }

    /**
     * Test ob eine Verbindung zur Datenbank besteht. Falls nicht, wird eine
     * Verbindung aufgebaut.
     * 
     * @return true, wenn Verbindung besteht bzw. eine Verbindung aufgebaut
     *         werden konnte, false sonst.
     */
    public boolean isOpen() {
	boolean open = false;
	try {
	    if (this.con.isClosed()) {
		open = openDB();
	    }
	} catch (SQLException e) {
	    return open;
	}
	return open;
    }

    /**
     * Baut eine Datenbank-Verbindung auf.
     * 
     * @return true, wenn eine Verbindung aufgebaut werden konnte, false sonst.
     */
    public boolean openDB() {
	try {
	    // Parameter für Verbindungsaufbau definieren

	    // JDBC-Treiber laden
	    Class.forName(driver);
	    // Verbindung aufbauen
	    con = DriverManager.getConnection(url, user, password);

	} catch (Exception ex) {
	    return false;
	}
	connectionStatus = true;
	return true;
    }

    /**
     * Schließt die Datenbank-Verbindung.
     * 
     * @return true, falls Verbindung geschlossen wurde, false sonst.
     */
    public boolean closeDB() {
	try {
	    if (!con.isClosed()) {
		con.close();
	    }
	} catch (SQLException e) {
	    return false;
	}
	connectionStatus = false;
	return true;
    }

    /**
     * Liefert die Verbindungs-Objekt.
     * 
     * @return Das Verbindungs-Objekt.
     */
    public Connection getCon() {
	return con;
    }

}
