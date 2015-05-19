package org.iviPro.editors.quiz.interfaces;

/**
 * Das Interface wird von der View und dem Controller benutzt.
 * 
 * @author Sabine Gattermann
 * 
 */
public interface LoginInterface {

    /**
     * Ueberpruefung der Zugangsdaten es Users.
     * 
     * @param userName
     *            Der Benutzername.
     * @param password
     *            Das Passwort.
     */
    void setUserLogin(String userName, String password);

    /**
     * Oeffnet die Registrierungs-View.
     */
    void openRegistration();

    /**
     * Oeffnet das Program nach erfolgreicher Anmeldung
     */
    void openMainApplication();

    /**
     * Uebergibt Meldungen an die View.
     * 
     * @return Der String der angezeigt werden soll.
     */
    String getMsg();

}
