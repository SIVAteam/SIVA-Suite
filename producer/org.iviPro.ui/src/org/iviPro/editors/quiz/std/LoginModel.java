package org.iviPro.editors.quiz.std;

import java.util.Observable;

import org.iviPro.editors.quiz.interfaces.LoginInterface;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;
import org.iviPro.editors.quiz.view.LoginView;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.User;

/**
 * Diese Klasse modelliert die Funktionalitaet zum Einloggen eines Benutzers.
 * 
 * @author Sabine Gattermann
 * 
 */
public class LoginModel extends Observable implements LoginInterface {

	// Status-Codes Observer-Pattern
	private static final int LOGIN = 1;
	private static final int PASS_ERROR = 2;
	private static final int NO_USER_ERROR = 3;

	// Fehlermeldungen
	private static final String PASS_ERROR_MSG = "falsches Passwort";
	private static final String NO_USER_ERROR_MSG = "Benutzer existiert nicht";

	// Variable fuer die aktuelle Fehlermeldung
	private String message = "";

	/**
	 * Standard-Konstruktor
	 */
	public LoginModel() {
		this.addObserver(new LoginView(QuizGenerator.getDefaultShell(), this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see login.LoginInterface#setUserLogin(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void setUserLogin(String userName, String password) {

		if (DbQueries.isUserInDB(userName)) {
			User x = DbQueries.getUserData(userName);
			if (x.getPassword().equals(password)) {
				super.setChanged();
				super.notifyObservers(LOGIN);
				QuizGenerator.setCurrentUser(userName);
			} else {
				message = PASS_ERROR_MSG;
				super.setChanged();
				super.notifyObservers(PASS_ERROR);
			}
		} else {
			message = NO_USER_ERROR_MSG;
			super.setChanged();
			super.notifyObservers(NO_USER_ERROR);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see login.LoginInterface#createNewUser()
	 */
	@Override
	public void openRegistration() {
		// false --> neues profil anlegen
		new RegistrationModel(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see login.LoginInterface#getMsg()
	 */
	@Override
	public String getMsg() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see login.LoginInterface#openMainApplication()
	 */
	@Override
	public void openMainApplication() {
		new MainModel();
	}

}
