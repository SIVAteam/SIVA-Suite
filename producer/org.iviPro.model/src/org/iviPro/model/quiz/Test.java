package org.iviPro.model.quiz;

import java.util.LinkedList;

import org.iviPro.model.IQuizBean;
import org.iviPro.model.Project;

/**
 * Klasse zum Verwalten eines Tests.
 * 
 * @author Sabine Gattermann
 * @modied Stefan Zwicklbauer
 * 
 * @uml.dependency supplier="org.iviPro.model.quiz"
 */
public class Test extends IQuizBean {

	private static final long serialVersionUID = 1L;

	/**
	 * @uml.property name="idTest"
	 */
	private int idTest;

	/**
	 * @uml.property name="title"
	 */
	private String title;

	/**
	 * @uml.property name="category"
	 */
	private String category;

	/**
	 * @uml.property name="description"
	 */
	private String description;

	/**
	 * @uml.property name="maxPoints"
	 */
	private int maxPoints;

	/**
	 * @uml.property name="evaluationMethod"
	 */
	private int evaluationMethod;

	/**
	 * @uml.property name="testType"
	 */
	private int testType;

	/**
	 * @uml.property name="timeOfFeedback"
	 */
	private int timeOfFeedback;

	/**
	 * @uml.property name="publicationStatus"
	 */
	private int publicationStatus;

	/**
	 * @uml.property name="idUser"
	 */
	private int idUser;

	/**
	 * Konstruktor
	 * 
	 * @param idTest
	 *            Die Test-ID.
	 * @param title
	 *            Der Titel.
	 * @param category
	 *            Die Kategororie.
	 * @param description
	 *            Die Beschreibung.
	 * @param maxPoints
	 *            Die max. Punkte.
	 * @param evaluationMethod
	 *            Das Bewertungssystem.
	 * @param testType
	 *            Der Testablauf.
	 * @param timeOfFeedback
	 *            Der Feedback-Zeitpunkt.
	 * @param publicationStatus
	 *            Die Freigabe.
	 * @param idUser
	 *            Die Benutzer-ID.
	 * @param nodeList
	 *            Die Knotenliste.
	 * @param edgeList
	 *            Die Kantenliste.
	 */
	public Test(Project project, int idTest, String title, String category,
			String description, int maxPoints, int evaluationMethod,
			int testType, int timeOfFeedback, int publicationStatus,
			int idUser, LinkedList<Node> nodeList, LinkedList<Edge> edgeList) {
		super(project);
		this.idTest = idTest;
		this.title = title;
		this.category = category;
		this.description = description;
		this.maxPoints = maxPoints;
		this.evaluationMethod = evaluationMethod;
		this.testType = testType;
		this.timeOfFeedback = timeOfFeedback;
		this.publicationStatus = publicationStatus;
		this.idUser = idUser;

	}

	/**
	 * Standard-Konstruktor.
	 */
	public Test(Project project, int curUser, int idTest) {
		super(project);
		System.out.println("HALLO ICH LEG EIN TESTOBJEKT MIT ID " + idTest);
		this.idTest = idTest;
		title = "Standardtest";
		category = "";
		description = "";
		maxPoints = 1;
		evaluationMethod = 0;
		testType = 0;
		timeOfFeedback = 0;
		publicationStatus = 0;
		idUser = curUser;
	}

	/**
	 * Getter fuer Test-ID.
	 * 
	 * @return Die ID.
	 */
	public int getIdTest() {
		return idTest;
	}

	/**
	 * Setter fuer Test-ID.
	 * 
	 * @param idTest
	 *            Die ID.
	 */
	public void setIdTest(int idTest) {
		this.idTest = idTest;
	}

	/**
	 * Getter fuer Titel.
	 * 
	 * @return Der Titel.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Setter fuer Titel.
	 * 
	 * @param title
	 *            Der Titel.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Getter fuer Kategorie.
	 * 
	 * @return Die Kategorie.
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Setter fuer Kategorie.
	 * 
	 * @param category
	 *            Die Kategorie.
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Getter fuer Beschreibung.
	 * 
	 * @return Die Beschreibung.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Setter fuer Beschreibung.
	 * 
	 * @param description
	 *            Die Beschreibung.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Getter fuer Ablauftyp.
	 * 
	 * @return 0, falls linear, 1, falls dynamisch.
	 */
	public int getTestType() {
		return testType;
	}

	/**
	 * Setter fuer Ablauftyp.
	 * 
	 * @param testType
	 *            0, falls linear, 1, falls dynamisch.
	 */
	public void setTestType(int testType) {
		this.testType = testType;
	}

	/**
	 * Getter fuer Bewertungssystem.
	 * 
	 * @return 0, fuer rein positiv, 1, wenn auch negative Punkte vergeben
	 *         werden.
	 */
	public int getEvaluationMethod() {
		return evaluationMethod;
	}

	/**
	 * Setter fuer Bewertungssystem.
	 * 
	 * @param evaluationMethod
	 *            0, fuer rein positiv, 1, wenn auch negative Punkte vergeben
	 *            werden.
	 */
	public void setEvaluationMethod(int evaluationMethod) {
		this.evaluationMethod = evaluationMethod;
	}

	/**
	 * Getter fuer max. Punkte.
	 * 
	 * @return Die max. Punkte.
	 */
	public int getMaxPoints() {
		return maxPoints;
	}

	/**
	 * Setter fuer max. Punkte.
	 * 
	 * @param maxPoints
	 *            Die max. Punkte.
	 */
	public void setMaxPoints(int maxPoints) {
		this.maxPoints = maxPoints;
	}

	/**
	 * Getter fuer Benutzer-ID.
	 * 
	 * @return Die ID.
	 */
	public int getIdUser() {
		return idUser;
	}

	/**
	 * Setter fuer Benutzer-ID.
	 * 
	 * @param benutzerIdUser
	 *            Die ID.
	 */
	public void setIdUser(int benutzerIdUser) {
		idUser = benutzerIdUser;
	}

	/**
	 * Setter fuer Freigabe (oeffentlich/privat)
	 * 
	 * @param publicationStatus
	 *            0, falls privat, 1, falls oeffentlich.
	 */
	public void setPublicationStatus(int publicationStatus) {
		this.publicationStatus = publicationStatus;
	}

	/**
	 * Getter fuer Freigabe (oeffentlich/privat)
	 * 
	 * @return 0, falls privat, 1, falls oeffentlich.
	 */
	public int getPublicationStatus() {
		return publicationStatus;
	}

	/**
	 * Setter fuer Feedback-Zeitpunkt.
	 * 
	 * @param timeOfFeedback
	 *            0, fuer am Ende, 1, fuer nach jeder Frage.
	 */
	public void setTimeOfFeedback(int timeOfFeedback) {
		this.timeOfFeedback = timeOfFeedback;
	}

	/**
	 * Getter fuer Feedback-Zeitpunkt.
	 * 
	 * @return 0, fuer am Ende, 1, fuer nach jeder Frage.
	 */
	public int getTimeOfFeedback() {
		return timeOfFeedback;
	}

	/**
	 * Getter fuer Typ als String.
	 * 
	 * @return Die Typ-Bezeichnung.
	 */
	public String getTestTypeString() {
		if (testType == 0)
			return "linear";
		else if (testType == 1)
			return "verzweigt";
		else
			return "unbekannt";
	}

	/**
	 * Getter fuer Bewertungssystem als String.
	 * 
	 * @return Die Bewertungssystem-Bezeichnung.
	 */
	public String getEvaluationMethodString() {
		if (evaluationMethod == 0)
			return "positiv";
		else if (testType == 1)
			return "positiv / negativ";
		else
			return "unbekannt";
	}

	/**
	 * Getter fuer Freigabe als String.
	 * 
	 * @return Die Freigabe-Bezeichnung.
	 */
	public String getPublicationStatusString() {
		if (publicationStatus == 0)
			return "privat";
		else if (publicationStatus == 1)
			return "öffentlich";
		else
			return "unbekannt";
	}

	/**
	 * Getter fuer Feedback als String.
	 * 
	 * @return Die Feedback-Bezeichnung.
	 */
	public String getTimeOfFeedbackString() {
		if (testType == 0)
			return "am Ende";
		else if (testType == 1)
			return "sofort";
		else
			return "unbekannt";
	}

	public boolean isDefault() {
		// return true if test still default, else false
		Test dummy = new Test(this.project, this.idUser, this.idTest);
		// compare with default dummy
		return this.equals(dummy);
	}

	@Override
	public boolean equals(Object other) {

		if (this == other) {
			return true;
		}

		if (other instanceof Test) {
			Test otherTest = (Test) other;
			return this.title.equals(otherTest.title)
					&& this.category.equals(otherTest.category)
					&& this.description.equals(otherTest.description)
					&& this.maxPoints == otherTest.maxPoints
					&& this.evaluationMethod == otherTest.evaluationMethod
					&& this.testType == otherTest.testType
					&& this.timeOfFeedback == otherTest.timeOfFeedback
					&& this.publicationStatus == otherTest.publicationStatus;
		}

		return false;
	}

}
