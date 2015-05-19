package org.iviPro.editors.quiz.interfaces;

import org.iviPro.model.quiz.User;

/**
 * Interface für Kommunikation zwischen Model und View beim Anlegen eines neuen
 * Benutzers
 * 
 * @author Sabine Gattermann
 * 
 */
public interface RegistationInterface {
    /**
     * Anlegen eines neuen Benutzers.
     * 
     * @param userName
     *            Der Benutzername.
     * @param password
     *            Das Passwort.
     * @param repeatedPassword
     *            Die Passwort-Wiederholung.
     * @param firstName
     *            Der Vorname.
     * @param lastName
     *            Der Nachname.
     * @param email
     *            Die Emailadresse.
     */
    void saveUser(String userName, String password, String repeatedPassword,
	    String firstName, String lastName, String email);

    /**
     * Abbruch der Registrierung.
     */
    void cancel();

    /**
     * Übergibt eine Nachricht für das Dialog-Fenster.
     * 
     * @return Die Dialog-Nachricht.
     */
    String getUIMsg();

    /**
     * Abfrage, ob neuer Benutzer angelegt, oder ein bestehendes Profil
     * bearbeitet werden soll.
     * 
     * @return true, falls ein neuer Benutzer angelegt werden soll, false, falls
     *         Profildaten geaendert werden sollen
     */
    boolean getIsNewUser();

    /**
     * Uebergibt den Benutzer.
     * 
     * @return Der Benutzer.
     */
    User getUserData();

    /**
     * Warnt den Benutzer mittels Popup-Window vor dem Loeschen seines Profils.
     */
    void warningDeleteProfil();

    /**
     * Loescht ein Profil. Oeffentliche Tests werden zuvor im DbBot importiert.
     */
    void deleteProfil();

}
