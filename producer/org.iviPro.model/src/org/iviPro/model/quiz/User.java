package org.iviPro.model.quiz;

import org.iviPro.model.IQuizBean;
import org.iviPro.model.Project;

/**
 * Klasse zum Verwalten eines Benutzers.
 * 
 * @author Sabine Gattermann
 * @modified Stefan Zwicklbauer
 * 
 */
public class User extends IQuizBean {

	private static final long serialVersionUID = 1L;

	/**
	 * @uml.property name="idUser"
	 */
	private int idUser = -1;

	/**
	 * @uml.property name="firstName"
	 */
	private String firstName = "";

	/**
	 * @uml.property name="LastName"
	 */
	private String lastName = "";

	/**
	 * @uml.property name="idTest"
	 */
	private String userName = "";

	/**
	 * @uml.property name="email"
	 */
	private String email = "";

	/**
	 * @uml.property name="password"
	 */
	private String password = "";

	/**
	 * Standard-Konstruktor.
	 */
	public User(Project project) {
		super(project);
	}

	public User(Project project, String firstName, String lastName,
			String userName, String email, String password) {
		super(project);
		this.firstName = firstName;
		this.lastName = lastName;
		this.userName = userName;
		this.email = email;
		this.password = password;
	}

	/**
	 * Getter fuer Vorname.
	 * 
	 * @return Der Vorname.
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Setter fuer Nachname.
	 * 
	 * @return Der Nachname.
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Getter fuer Username.
	 * 
	 * @return Der Username.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Getter fuer Emailadresse.
	 * 
	 * @return Die Emailadresse.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Getter fuer Passwort.
	 * 
	 * @return Das Passwort.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Setter fuer Vorname.
	 * 
	 * @param firstName
	 *            Der Vorname.
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Setter fuer Nachname.
	 * 
	 * @param lastName
	 *            Der Nachname.
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Setter fuer Username.
	 * 
	 * @param userName
	 *            Der Username.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Setter fuer Emailadresse.
	 * 
	 * @param email
	 *            Die Emailadresse.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Setter fuer das Passwort.
	 * 
	 * @param password
	 *            Das Passwort.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Getter fuer User-ID.
	 * 
	 * @return Die User-ID.
	 */
	public int getIdUser() {
		return this.idUser;
	}

	/**
	 * Setter fuer User-ID.
	 * 
	 * @param idUser
	 *            Die User-ID.
	 */
	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

}
