package org.iviPro.editors.quiz.std;

import java.util.LinkedList;
import java.util.Observable;

import org.iviPro.application.Application;
import org.iviPro.editors.quiz.interfaces.TestInterface;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;
import org.iviPro.editors.quiz.view.TestView;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Edge;
import org.iviPro.model.quiz.Node;
import org.iviPro.model.quiz.Test;

/**
 * Klasse zum Erstellen und Aendern von Testeigenschaften.
 * 
 * @author Sabine Gattermann
 * 
 */
public class TestModel extends Observable implements TestInterface {

	// Status-Codes Observer-Pattern
	private static final int START = 1;
	private static final int TESTNAME_EXISTS_ERROR = 2;
	private static final int TESTNAME_EMPTY_ERROR = 3;
	private static final int POINTS_NEGATIVE_ERROR = 4;
	private static final int POINTS_PARSE_ERROR = 5;

	// Fehlermeldungen
	private static final String TESTNAME_EXISTS_ERROR_MSG = "Titel existiert bereits, \nbitte anderen wählen!";
	private static final String TESTNAME_EMPTY_ERROR_MSG = "Bitte geben Sie einen Titel an!";
	private static final String POINTS_NEGATIVE_ERROR_MSG = "Max.-Punkte müssen >= 1 sein!";
	private static final String POINTS_PARSE_ERROR_MSG = "Bitte geben Sie die \nmaximalen Punkte korrekt an!";
	private static final int DISABLE = 0;
	private static final int ENABLE = 1;

	// Variable fuer maxpunkte-parsing
	private int punkte;

	// Variable fuer die aktuelle Fehlermeldung
	private String message;
	private Test currentTest;

	private int idTest;
	private String title;
	private String kategorie;
	private String beschreibung;
	private int maxPunkte;
	private int bewertungssystem;
	private int ablauf;
	private boolean random;
	private int feedback;
	private int testInPublicTestpool;
	private int idBenutzer;
	private boolean isNewTest;

	// neuen Test erstellen: 1 / Test aus DB laden: 0
	private int status;

	/**
	 * Konstruktor zum Erstellen einen neuen Tests
	 */
	public TestModel(int testId) {
		currentTest = new Test(Application.getCurrentProject(), QuizGenerator
				.getCurrentUser().getIdUser(), testId);
		this.status = ENABLE;
		initVariables();
		this.addObserver(new TestView(QuizGenerator.getDefaultShell(), this));
		this.isNewTest = true;
		QuizGenerator.getMainModelInstance().setTestModelInstance(this);

	}

	/**
	 * Konstruktor zum Laden eines bestehenden Tests
	 */
	public TestModel(Test test) {
		currentTest = test;
		this.status = DISABLE;
		initVariables();
		if (test.getIdUser() == -1) {
			test.setIdUser(QuizGenerator.getCurrentUser().getIdUser());
		}
		this.addObserver(new TestView(QuizGenerator.getDefaultShell(), this));
		this.isNewTest = false;
		QuizGenerator.getMainModelInstance().setTestModelInstance(this);
	}

	/**
	 * Initialisierung
	 */
	private void initVariables() {
		this.idTest = currentTest.getIdTest();
		this.title = currentTest.getTitle();
		this.kategorie = currentTest.getCategory();
		this.beschreibung = currentTest.getDescription();
		this.maxPunkte = currentTest.getMaxPoints();
		this.bewertungssystem = currentTest.getEvaluationMethod();
		this.ablauf = currentTest.getTestType();
		this.random = true;
		this.feedback = currentTest.getTimeOfFeedback();
		this.testInPublicTestpool = currentTest.getPublicationStatus();
		this.idBenutzer = currentTest.getIdUser();
		QuizGenerator.setCurrentTest(currentTest);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#cancel()
	 */
	@Override
	public void cancel() {
		// new MainModel();
		if (isNewTest) {
			new MainModel();
		} else {
			new TestManagementModel();
		}
		QuizGenerator.getMainModelInstance().setTestModelInstance(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#startTestGeneration(boolean)
	 */
	@Override
	public void startTestGeneration(boolean loadFromDB) {
//		new NodeModel(currentTest, loadFromDB, random);
		QuizGenerator.getMainModelInstance().setTestModelInstance(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#getMsg()
	 */
	@Override
	public String getMsg() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#checkUserInputFirst(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, int, int, int, int)
	 */
	@Override
	public void checkUserInputFirst(String titel, String kategorie,
			String beschreibung, String maxpunkte, int bewertungssystem,
			int aufbau, boolean random, int feedback, int istPrivaterTest) {

		if (titel.length() == 0) {
			message = TESTNAME_EMPTY_ERROR_MSG; // kein Titel eigegeben
			setChanged();
			notifyObservers(TESTNAME_EMPTY_ERROR);
		} else if (testNameAlreadyExists(titel)) {
			message = TESTNAME_EXISTS_ERROR_MSG; // Titel bereits vorhanden
			setChanged();
			notifyObservers(TESTNAME_EXISTS_ERROR);
		} else if (!pointsParsingOkay(maxpunkte)) {
			message = POINTS_PARSE_ERROR_MSG; // max. Punkte: Parsing-Fehler
			setChanged();
			notifyObservers(POINTS_PARSE_ERROR);
		} else if (punkte <= 0) {
			message = POINTS_NEGATIVE_ERROR_MSG; // max. Punkte < 0
			setChanged();
			notifyObservers(POINTS_NEGATIVE_ERROR);
		} else {
			currentTest.setTitle(titel); // Eingaben okay
			currentTest.setCategory(kategorie);
			currentTest.setDescription(beschreibung);
			currentTest.setMaxPoints(this.punkte);
			currentTest.setEvaluationMethod(bewertungssystem);
			currentTest.setTestType(aufbau);
			currentTest.setTimeOfFeedback(feedback);
			currentTest.setPublicationStatus(istPrivaterTest);
			currentTest.setIdUser(QuizGenerator.getCurrentUser().getIdUser());

			this.random = random;
			QuizGenerator.setCurrentTest(currentTest);
			int id = DbQueries.setTestData(currentTest);
			QuizGenerator.getCurrentTest().setIdTest(id);

			setChanged();
			notifyObservers(START);
		}

	}

	/**
	 * Ueberprueft, ob sich der Titel bereits in der Datenbank befindet (in
	 * Kombination mit Benutzer)
	 * 
	 * @param testName
	 *            Der Titel.
	 * @return true, falls Titel existiert, false sonst
	 */
	private boolean testNameAlreadyExists(String testName) {
		if (!DbQueries.isTestNameInPrivateDB(testName, QuizGenerator
				.getCurrentUser().getIdUser())) {
			return false;
		}
		return true;
	}

	/**
	 * Testet, ob String auf Integer geparsed werden kann (zwecks Leerstring
	 * oder 'nicht Ziffern'). Wenn es moeglich ist, wird der geparste Wert in
	 * 'punkte' gespeichert.
	 * 
	 * @param points
	 *            Der String.
	 * @return true, falls Parsing moeglich, false sonst
	 */
	private boolean pointsParsingOkay(String points) {
		if (points.length() > 0) {
			try {
				this.punkte = Integer.parseInt(points);
			} catch (NumberFormatException e) {
				message = POINTS_PARSE_ERROR_MSG;
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Ueberprueft, ob Testeigenschaften geaendert wurden.
	 * 
	 * @param kategorie
	 *            Die Kategorie.
	 * @param beschreibung
	 *            Die Beschreibung.
	 * @param maxpunkte
	 *            Die max. Punkte.
	 * @param bewertungssystem
	 *            Das Bewertungssystem.
	 * @param aufbau
	 *            Der Testaufbau.
	 * @param feedback
	 *            Der Feedback-Zeitpunkt.
	 * @param istPrivaterTest
	 *            Die Zugehörigkeit zum öffentlichen Testpool.
	 * @return false, wenn nichts geaendert wurde, true sonst.
	 */
	private boolean propertiesHaveChanged(String kategorie,
			String beschreibung, String maxpunkte, int bewertungssystem,
			int aufbau, int feedback, int istPrivaterTest) {

		if (!kategorie.equals(currentTest.getCategory())) {
			return true;
		} else if (!beschreibung.equals(currentTest.getDescription())) {
			return true;
		} else if (!maxpunkte.equals(currentTest.getMaxPoints())) {
			return true;
		} else if (bewertungssystem != currentTest.getEvaluationMethod()) {
			return true;
		} else if (aufbau != currentTest.getTestType()) {
			return true;
		} else if (feedback != currentTest.getTimeOfFeedback()) {
			return true;
		} else if (istPrivaterTest != currentTest.getPublicationStatus()) {
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#checkForEditedTestProperties(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, int, int, int, int)
	 */
	@Override
	public void checkForEditedTestProperties(String titel, String kategorie,
			String beschreibung, String maxpunkte, int bewertungssystem,
			int aufbau, boolean random, int feedback, int istPrivaterTest) {
		if (titel.length() == 0) {
			message = TESTNAME_EMPTY_ERROR_MSG;
			setChanged();
			notifyObservers(TESTNAME_EMPTY_ERROR);
		} else {
			if (!titel.equals(currentTest.getTitle())
					&& DbQueries.isTestNameInPrivateDB(titel,
							currentTest.getIdUser())) {
				message = TESTNAME_EXISTS_ERROR_MSG;
				setChanged();
				notifyObservers(TESTNAME_EXISTS_ERROR);
			} else if (!pointsParsingOkay(maxpunkte)) {
				message = POINTS_PARSE_ERROR_MSG;
				setChanged();
				notifyObservers(POINTS_PARSE_ERROR);
			} else if (punkte <= 0) {
				message = POINTS_NEGATIVE_ERROR_MSG;
				setChanged();
				notifyObservers(POINTS_NEGATIVE_ERROR);
			} else {

				currentTest = new Test(Application.getCurrentProject(), idTest,
						titel, kategorie, beschreibung,
						Integer.parseInt(maxpunkte), bewertungssystem, aufbau,
						feedback, istPrivaterTest, idBenutzer,
						new LinkedList<Node>(), new LinkedList<Edge>());
				currentTest.setIdUser(idBenutzer);

				if (!titel.equals(currentTest.getTitle())) {
					DbQueries.updateTestData(currentTest);
				} else if (propertiesHaveChanged(kategorie, beschreibung,
						maxpunkte, bewertungssystem, aufbau, feedback,
						istPrivaterTest)) {
					DbQueries.updateTestData(currentTest);
				}

				this.random = random;
				setChanged();
				notifyObservers(START);
			}
		}

	}

	// Getter & Setter

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#setBewertungssystem(int)
	 */
	@Override
	public void setEvaluationMethod(int bewertungssystem) {
		this.bewertungssystem = bewertungssystem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#getBewertungssystem()
	 */
	@Override
	public int getEvaluationMethod() {
		return bewertungssystem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#getIdTest()
	 */
	@Override
	public int getIdTest() {
		return idTest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#setIdTest(int)
	 */
	@Override
	public void setIdTest(int idTest) {
		this.idTest = idTest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#getKategorie()
	 */
	@Override
	public String getCategory() {
		return kategorie;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#setKategorie(java.lang.String)
	 */
	@Override
	public void setCategory(String kategorie) {
		this.kategorie = kategorie;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#getBeschreibung()
	 */
	@Override
	public String getDescription() {
		return beschreibung;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#setBeschreibung(java.lang.String)
	 */
	@Override
	public void setDescription(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#getAblauf()
	 */
	@Override
	public int getTestType() {
		return ablauf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#setAblauf(int)
	 */
	@Override
	public void setTestType(int ablauf) {
		this.ablauf = ablauf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#getMaxPunkte()
	 */
	@Override
	public int getMaxPoints() {
		return maxPunkte;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#setMaxPunkte(int)
	 */
	@Override
	public void setMaxPoints(int maxPunkte) {
		this.maxPunkte = maxPunkte;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#getIdBenutzer()
	 */
	@Override
	public int getIdUser() {
		return idBenutzer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#setIdBenutzer(int)
	 */
	@Override
	public void setIdUser(int benutzerIdBenutzer) {
		idBenutzer = benutzerIdBenutzer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#getKategorieList()
	 */
	@Override
	public String[] getCategoryList() {
		String[] list = DbQueries.getUserCategorzList();
		if (list == null) {
			list = new String[1];
			list[0] = "";
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#getFeedback()
	 */
	@Override
	public int getTimeOfFeedback() {
		return feedback;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#setFeedback(int)
	 */
	@Override
	public void setTimeOfFeedback(int feedback) {
		this.feedback = feedback;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#getTestInPublicTestpool()
	 */
	@Override
	public int getTestInPublicTestpool() {
		return testInPublicTestpool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.TestInterface#setTestInPublicTestpool(int)
	 */
	@Override
	public void setPublicationStatus(int testInPublicTestpool) {
		this.testInPublicTestpool = testInPublicTestpool;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public boolean isRandom() {
		return random;
	}

	@Override
	public void setRandom(boolean isRandom) {
		this.random = isRandom;

	}

}
