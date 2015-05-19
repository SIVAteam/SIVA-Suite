package org.iviPro.editors.quiz.std;

import java.util.LinkedList;
import java.util.Observable;

import org.iviPro.application.Application;
import org.iviPro.editors.quiz.interfaces.RegistationInterface;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;
import org.iviPro.editors.quiz.view.RegistrationView;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Test;
import org.iviPro.model.quiz.User;

/**
 * Klasse zum Anlegen eines neuen Benutzers bzw. zum Aendern von Benutzerdaten.
 * 
 * @author Sabine Gattermann
 * 
 */
public class RegistrationModel extends Observable implements
		RegistationInterface {

	// Konstanten für Kommunikation mit der View:
	// nicht alle Felder ausgefüllt
	private static final int FIELDS = 1;
	// Benutzername bereits vergeben
	private static final int USER = 2;
	// Passwortwiederholung falsch
	private static final int PASS = 3;
	// keine gültige Emailadresse
	private static final int MAIL = 4;
	// alles korrekt ausgefüllt
	private static final int SUCCESS = 5;
	// Passwort zu kurz
	private static final int PASSLENGTH = 6;

	private static final int WARNING = 7;

	// Variablen zum Speichern der Benutzereingaben
	private String userName;

	// Variable für Error-Dialog der View
	private String message = "";
	// Konstanten für Error-Dialog
	private final String PASSWORD_ERROR_MSG = "Passwort und Passwort-Wiederholung sind nicht identisch.";
	private static final String PASSWORDLENGTH_ERROR_MSG = "Ihr Passwort muss mindestens 6 Zeichen umfassen.";
	private final String NOTFILLED_ERROR_MSG = "Bitte füllen Sie alle Felder aus.";
	private final String USEREXISTS_ERROR_MSG = "Der gewünschte Benutzername ist bereits vergeben.";
	private final String EMAIL_ERROR_MSG = "Bitte geben Sie eine korrekte Emailadresse an.";
	private final String USER_CREATED_MSG = "Ihre Registrierung war erfolgreich.";

	private boolean isNewUser;

	/**
	 * Konstruktor
	 * 
	 * @param isNew
	 *            true, fuer neuen Benutzer, false, fuer das Aendern von
	 *            Benutzerdaten
	 */
	public RegistrationModel(boolean isNew) {

		this.isNewUser = isNew;
		this.addObserver(new RegistrationView(QuizGenerator.getDefaultShell(),
				this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see registration.RegistationInterface#saveUser(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void saveUser(String userName, String password,
			String repeatedPassword, String firstName, String lastName,
			String email) {

		this.userName = userName;

		// alles ausgefuellt?
		if (!checkFilledOut(userName, password, repeatedPassword, firstName,
				lastName, email)) {
			message = NOTFILLED_ERROR_MSG;
			super.setChanged();
			super.notifyObservers(FIELDS);
		} else if (isNewUser == true && !checkUserName()) { // Benutzername
			// okay?
			message = USEREXISTS_ERROR_MSG;
			super.setChanged();
			super.notifyObservers(USER);
		} else if (!checkPasswordLength(password)) { // Passwortlaenge okay?
			message = PASSWORDLENGTH_ERROR_MSG;
			super.setChanged();
			super.notifyObservers(PASSLENGTH);
		} else if (!checkPassword(password, repeatedPassword)) { // Passwoerter
			// okay?
			message = PASSWORD_ERROR_MSG;
			super.setChanged();
			super.notifyObservers(PASS);
		} else if (!checkEmail(email)) { // Email okay?
			message = EMAIL_ERROR_MSG;
			super.setChanged();
			super.notifyObservers(MAIL);
		} else {
			if (isNewUser == true) {
				// Benutzer in DB speichern
				DbQueries.setUserData(userName, password, firstName, lastName,
						email);
				message = USER_CREATED_MSG;
				super.setChanged();
				super.notifyObservers(SUCCESS);
			} else {
				// Benutzerdaten aktualisieren
				DbQueries.updateUserData(userName, password, firstName,
						lastName, email);
				QuizGenerator.setCurrentUser(userName);
				super.setChanged();
				super.notifyObservers(SUCCESS);
			}

		}

	}

	/**
	 * Laenge des Passworts ueberpruefen. Laenge muss zwischen 6 und 12 liegen.
	 * 
	 * @param password
	 *            Das Passwort.
	 * @return true, okay, fals, sonst
	 */
	private boolean checkPasswordLength(String password) {
		if (password.length() >= 6 && password.length() <= 12)
			return true;
		return false;
	}

	/**
	 * Ueberprueft, ob alle Felder ausgefuellt wurden.
	 * 
	 * @param email
	 *            Der Email-Input.
	 * @param lastName
	 *            Der Nachname-Input.
	 * @param firstName
	 *            Der Vorname-Input.
	 * @param password
	 *            Der Passwort1-Input.
	 * @param repeatedPassword
	 *            Der Passwort2-Input.
	 * @param userName
	 *            Der Benutzername-Input.
	 * 
	 * @return true, falls ja, false sonst.
	 */
	private boolean checkFilledOut(String userName, String password,
			String repeatedPassword, String firstName, String lastName,
			String email) {
		if (userName.length() == 0 || password.length() == 0
				|| repeatedPassword.length() == 0 || firstName.length() == 0
				|| lastName.length() == 0 || email.length() == 0)
			return false;
		return true;
	}

	/**
	 * Testet ob es sich um ein gueltiges Email-Format handelt.
	 * 
	 * @param email
	 *            Die Email-Adresse.
	 * @return true, falls ja, false sonst.
	 */
	private boolean checkEmail(String email) {
		// try {
		// InternetAddress addi = new InternetAddress(email);
		// addi.validate();
		// } catch (AddressException e) {
		// return false;
		// }
		return true;
	}

	/**
	 * Testet, ob Passwort und Passwort-Wiederholung identisch sind
	 * 
	 * @param password
	 *            Das Passwort.
	 * @param repeatedPassword
	 *            Die Wiederholung des Passworts.
	 * 
	 * @return true, falls identisch, false sonst.
	 */
	private boolean checkPassword(String password, String repeatedPassword) {

		if (!password.equals(repeatedPassword))
			return false;
		return true;
	}

	/**
	 * Testet, ob der Benutzername bereits vergeben ist.
	 * 
	 * @return true, falls er noch nicht vergeben ist, false sonst
	 */
	private boolean checkUserName() {
		if (DbQueries.isUserInDB(userName))
			return false;
		else
			return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see registration.RegistationInterface#cancel()
	 */
	@Override
	public void cancel() {
		if (isNewUser)
			new LoginModel();
		else
			new MainModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see registration.RegistationInterface#getUIMsg()
	 */
	@Override
	public String getUIMsg() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see registration.RegistationInterface#getIsNewUser()
	 */
	@Override
	public boolean getIsNewUser() {
		return isNewUser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see registration.RegistationInterface#getUserData()
	 */
	@Override
	public User getUserData() {

		return QuizGenerator.getCurrentUser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see registration.RegistationInterface#warningDeleteProfil()
	 */
	@Override
	public void warningDeleteProfil() {
		message = "Vorsicht! \n\nIhr Profil wird sofort und unwiederruflich gelöscht! \n\n"
				+ "Um zum Programm zum Programm zurückzukehren, drücken Sie bitte ABBRECHEN !\n\n\n\n";
		setChanged();
		notifyObservers(WARNING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see registration.RegistationInterface#deleteProfil()
	 */
	@Override
	public void deleteProfil() {
		LinkedList<Test> publicTests = DbQueries.getUserTests(QuizGenerator
				.getCurrentUser());
		for (int i = 0; i < publicTests.size(); i++) {
			if (publicTests.get(i).getPublicationStatus() == 1) {
				// oeffentliche Test nach DbBot-Benutzer kopieren
				String importTitle = publicTests.get(i).getTitle();
				LinkedList<String> botTests = DbQueries
						.getTestTitlesByUsername(QuizGenerator
								.getDatabaseBotName());
				while (botTests.contains(importTitle))
					importTitle = importTitle + "1";

				Test importedTest = DbQueries
						.importTest(
								DbQueries.getUserData(
										QuizGenerator.getDatabaseBotName())
										.getIdUser(), publicTests.get(i)
										.getIdTest(), importTitle, Application
										.getCurrentProject());
				QuizGenerator.setCurrentTest(importedTest);
			}
		}

		DbQueries.deleteUser(QuizGenerator.getCurrentUser());

		QuizGenerator.getMainModelInstance().close();

	}

}
