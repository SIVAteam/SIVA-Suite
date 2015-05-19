package org.iviPro.editors.quiz.interfaces;

/**
 * Interface für Kommunikation zwischen Model und View beim Anlegen eines neuen
 * Benutzers
 * 
 * @author Sabine Gattermann
 * 
 */
public interface TestInterface {

    /**
     * Ueberpruefung der Benutzer-Eingaben.
     * 
     * @param title
     *            Der Titel.
     * @param category
     *            Die Kategorie.
     * @param description
     *            Die Beschreibung.
     * @param maxPoints
     *            Die max. Punkte.
     * @param evaluationMethod
     *            Das Bewertungssystem.
     * @param testType
     *            Der Testaufbau.
     * @param random
     *            Randomisierung von Antworten.
     * @param timeOfFeedback
     *            Der Feedback-Zeitpunkt.
     * @param publicationStatus
     *            Der Veroeffentlichungs-Status.
     */
    public void checkUserInputFirst(String title, String category,
	    String description, String maxPoints, int evaluationMethod,
	    int testType, boolean random, int timeOfFeedback,
	    int publicationStatus);

    /**
     * Ueberprueft, ob der Testeigenschaften durch den Benutzer geaendert
     * wurden.
     * 
     * @param title
     *            Der Titel.
     * @param category
     *            Die Kategorie.
     * @param description
     *            Die Beschreibung.
     * @param maxPoints
     *            Die max. Punkte.
     * @param evaluationMethod
     *            Das Bewertungssystem.
     * @param testType
     *            Der Testaufbau.
     * @param random
     *            Randomisierung von Antworten.
     * @param timeOfFeedback
     *            Der Feedback-Zeitpunkt.
     * @param publicationStatus
     *            Der Veroeffentlichungs-Status.
     */
    public void checkForEditedTestProperties(String title, String category,
	    String description, String maxPoints, int evaluationMethod,
	    int testType, boolean random, int timeOfFeedback,
	    int publicationStatus);

    /**
     * Startet das Eigeben einzelner Fragen.
     * 
     * @param loadFromDB
     *            true, falls ein Test bearbeitet wird, false, wenn ein neuer
     *            Test angelegt wird.
     */
    public void startTestGeneration(boolean loadFromDB);

    /**
     * Backbutton-Funktionalitaet. (zurueck zur Hauptmenue oder zurueck zum
     * privaten Testpool)
     */
    public void cancel();

    /**
     * Abfrage, ob es sich um einen neuen oder einen bestehenen Test handelt.
     * 
     * @return 1, falls neuer Test (ENABLE), 0 falls geladener Test (DISABLE)
     */
    int getStatus();

    /**
     * Getter fuer Nachrichten an den Benutzer.
     * 
     * @return Den Nachrichten-String.
     */
    public String getMsg();

    /**
     * Setter fuer Bewertungssystem-Eigenschaft.
     * 
     * @param evaluationMethod
     *            0, fuer keine negativen Punkte, 1 fuer negative Punkte
     *            moeglich.
     */
    void setEvaluationMethod(int evaluationMethod);

    /**
     * Getter fuer Bewertungssystem-Eigenschaft.
     * 
     * @return bewertungssystem 0, fuer keine negativen Punkte, 1 fuer negative
     *         Punkte moeglich.
     */
    int getEvaluationMethod();

    /**
     * Getter fuer Test-ID.
     * 
     * @return Die ID.
     */
    int getIdTest();

    /**
     * Setter fuer Test-ID.
     * 
     * @param idTest
     *            Die ID.
     */
    void setIdTest(int idTest);

    /**
     * Getter fuer Test-Titel.
     * 
     * @return Der Titel.
     */
    String getTitle();

    /**
     * Setter fuer Test-Titel.
     * 
     * @param title
     *            Der Titel.
     */
    void setTitle(String title);

    /**
     * Getter fuer Kategorie.
     * 
     * @return Die Kategorie.
     */
    String getCategory();

    /**
     * Setter fuer Kategorie.
     * 
     * @param category
     *            Die Kategorie.
     */
    void setCategory(String category);

    /**
     * Getter fuer Beschreibung des Tests.
     * 
     * @return Die Beschreibung.
     */
    String getDescription();

    /**
     * Setter fuer die Beschreibung des Tests.
     * 
     * @param description
     *            Die Beschreibung.
     */
    void setDescription(String description);

    /**
     * Getter fuer den Testablauf.
     * 
     * @return 0, fuer linear, 1, fuer dynamisch.
     */
    int getTestType();

    /**
     * Setter fuer den Testablauf.
     * 
     * @param testType
     *            0, fuer linear, 1, fuer dynamisch.
     */
    void setTestType(int testType);

    /**
     * Getter fuer MaxPunkte
     * 
     * @return Die Punkte.
     */
    int getMaxPoints();

    /**
     * Setter fuer MaxPunkte.
     * 
     * @param maxPoints
     *            Die Punkte.
     */
    void setMaxPoints(int maxPoints);

    /**
     * Getter fuer ID des Benutzers.
     * 
     * @return Die ID.
     */
    int getIdUser();

    /**
     * Setter fuer ID des Benutzers.
     * 
     * @param userId
     *            Die ID.
     */
    void setIdUser(int userId);

    /**
     * Getter fuer eine Auswahlliste von Kategorien.
     * 
     * @return Die Liste von Kategorien.
     */
    String[] getCategoryList();

    /**
     * Getter fuer Feedback-Zeitpunkt.
     * 
     * @return 0, fuer am Ende, 1 fuer sofort.
     */
    int getTimeOfFeedback();

    /**
     * Setter fuer Feedback-Zeitpunkt.
     * 
     * @param timeOfFeedback
     *            0, fuer am Ende, 1 fuer sofort.
     */
    void setTimeOfFeedback(int timeOfFeedback);

    /**
     * Getter fuer Veroeffentlichungs-Status.
     * 
     * @return 0, falls privat, 1 falls oeffentlich
     */
    int getTestInPublicTestpool();

    /**
     * Setter fuer Veroeffentlichungs-Status.
     * 
     * @param publicationStatus
     *            0, falls privat, 1 falls oeffentlich
     */
    void setPublicationStatus(int publicationStatus);

    /**
     * Getter fuer Randomisierung.
     * 
     * @return true, falls aktiviert, false sonst
     */
    boolean isRandom();

    /**
     * Setter fuer Randomisierung.
     * 
     * @param isRandom
     *            true, falls aktiviert, false sonst.
     */
    void setRandom(boolean isRandom);
}
