package org.iviPro.editors.quiz.interfaces;

import java.util.LinkedList;

import org.iviPro.model.quiz.AdditionalInfo;
import org.iviPro.model.quiz.Answer;
import org.iviPro.model.quiz.Condition;
import org.iviPro.model.quiz.Edge;
import org.iviPro.model.quiz.Node;
import org.iviPro.model.quiz.Question;

/**
 * Das Interface wird von der View und dem Controller benutzt.
 * 
 * @author Sabine Gattermann
 * 
 */
public interface NodeInterface {

    /**
     * Beendet die Testgenerierung.
     */
    void exit();

    /**
     * Fragt ab, ob die Testgenerierung abgeschlossen werden kann.
     * 
     * @return true, falls ja, false sonst.
     */
    boolean getCanExit();

    /**
     * Fragt ab, ob der aktuelle Knoten einen Vorgaenger hat.
     * 
     * @return true, falls ja, false sonst.
     */
    boolean enableLastNode();

    /**
     * Fragt ab, ob der aktuelle Knoten einen Nachfolger hat.
     * 
     * @return true, falls ja, false sonst.
     */
    boolean enableNextNode();

    /**
     * Fragt ab, ob dem aktuellen Knoten noch eine Kante hinzugefuegt werden
     * kann.
     * 
     * @return true, falls ja, false sonst.
     */
    boolean canInsertAnotherEdge();

    /**
     * Uebergibt eine Liste aller Fragen.
     * 
     * @return Die Liste aller Fragen.
     */
    LinkedList<Question> getQuestions();

    /**
     * Uebergibt den Titel des Tests.
     * 
     * @return Der Titel.
     */
    String getTestTitle();

    /**
     * Fragt ab, um welchen InsertAlgorithmus es sich handelt.
     * 
     * @return 0, falls linear, 1 falls dynamisch.
     */
    int getInsertAlgorithm();

    /**
     * Uebergibt eine Punkt-Liste fuer das Bedingungs-Punkte-Feld.
     * 
     * @return Die Punkte-Liste.
     */
    String[] getPointsForConditionField();

    /**
     * Uebergibt eine Lookback-Liste fuer das Bedingungs-Lookback-Feld.
     * 
     * @param positionNode
     *            des betreffenden Knotens in der Knotenliste.
     * @return Liste aller moeglichen Lookback-Werte.
     */
    String[] getLookbackForConditionField(int positionNode);

    /**
     * Setzt den Lookback.
     * 
     * @param lookback
     *            Der Lookback.
     */
    void setLookback(int lookback);

    /**
     * Uebergibt die aktuelle Bedingung.
     * 
     * @return Die Bedingung.
     */
    Condition getCurrentCondition();

    /**
     * Uebergibt eine Liste mit moeglichen Lookback-Werten fuer einen bestimmten
     * Knoten.
     * 
     * @param nodeNumber
     *            Die Position des Knoten in der KnotenListe des Models.
     * @return Die Lookback-Liste.
     */
    String[] getPossibleLookback(int nodeNumber);

    /**
     * Uebergibt eine Liste mit moeglichen Punkte-Bedingungs-Werten fuer einen
     * Lookback.
     * 
     * @param positionNode1
     *            Position des Quellknotens in der Knotenliste.
     * @param positionNode2
     *            Position des Zielknotens in der Knotenliste.
     * @param lookback
     *            Der spezielle Lookback.
     * @return Die Punkte-Bedingungs-Liste.
     */
    String[] getPossiblePoints(int positionNode1, int positionNode2,
	    int lookback);

    /**
     * Kanten-Update.
     * 
     * @param positionSourceInNodeList
     *            Die Position des Quellknotens in der KnotenListe des Models.
     * @param positionDestinationInNodeList
     *            Die Position des Zielknotens in der KnotenListe des Models.
     * @param conditionPoints
     *            Die Punkte.
     * @param conditionLookback
     *            Der Lookback.
     */
    void updateEdge(int positionSourceInNodeList,
	    int positionDestinationInNodeList, int conditionPoints,
	    int conditionLookback);

    /**
     * Einfuegen einer neuen Kante.
     * 
     * @param positionSourceInNodeList
     *            Die Position des Quellknotens in der KnotenListe des Models.
     * @param positionDestinationInNodeList
     *            Die Position des Zielknotens in der KnotenListe des Models.
     * @param conditionLookback
     *            Der Lookback.
     * @param conditionPoins
     *            Die Punkte.
     */
    void addNewEdge(int positionSourceInNodeList,
	    int positionDestinationInNodeList, int conditionLookback,
	    int conditionPoins);

    /**
     * Loeschen einer Kante.
     * 
     * @param positionSourceInNodeList
     *            Die Position des Quellknotens in der KnotenListe des Models.
     * @param positionDestinationInNodeList
     *            Die Position des Zielknotens in der KnotenListe des Models.
     */
    void deleteEdge(int positionSourceInNodeList,
	    int positionDestinationInNodeList);

    /**
     * Uebergibt die Listen zum Fuellen der ComboBoxes der
     * Kanten/Bedingungs-View-Teils (Kante: von Frage x nach Frage y)
     * 
     * @return Die Liste.
     */
    LinkedList<String> getEdgeNumbersForGui();

    /**
     * Uebergibt an das Model, was der Benutzer im Zest-Graphen angeklickt hat.
     * 
     * @param from
     *            Die Position des Quellknotens in der (Model-)KnotenListe.
     * @param to
     *            Die Position des Zielknotens in der (Model-)KnotenListe.
     * @param edge
     *            Die Kante.
     * @param doOnlyLayoutEdge
     *            Indikator, ob nur ein Update der Kante (true) oder des ganzen
     *            Knotens (false) durchgefuehrt werden soll.
     */
    void setEdgeNumbersForGui(int from, int to, Edge edge,
	    boolean doOnlyLayoutEdge);

    /**
     * Abfrage der aktuellen Position.
     * 
     * @return Die Position.
     */
    int getCurrentPosition();

    /**
     * Abfrage einer Nachricht.
     * 
     * @return Die Nachricht.
     */
    String getMessage();

    /**
     * Loeschen des aktuellen Knotens.
     */
    void deleteNode();

    /**
     * Abfrage der Knotenliste.
     * 
     * @return Die Knotenliste.
     */
    LinkedList<Node> getNodeList();

    /**
     * Abfrage der Kantenliste.
     * 
     * @return Die Kantenliste.
     */
    LinkedList<Edge> getEdgeList();

    /**
     * Update eines Knotens.
     * 
     * @param questionText
     *            Der Fragetext.
     * @param points
     *            Die Punkte.
     * @param answerList
     *            Die Liste der Antworttexte.
     * @param boolAnswerList
     *            Die korrekt-Liste der Antworten.
     * @param random
     *            Indikator fuer Randomisierung der Antworten.
     * @param linkList
     *            Die Link-Liste.
     * @param videoList
     *            Die Video-Liste.
     * @param audioList
     *            Die Audio-Liste.
     * @param imageList
     *            Die Image-Liste.
     * @param doAction
     *            Indikator fuer die Aktion nach dem Speichern (beenden = 0,
     *            neue Frage = 21, andere Frage laden = 22, neuen Knoten
     *            einfuegen = 23)
     */
    void updateDbNode(String questionText, int points,
	    LinkedList<String> answerList, LinkedList<Boolean> boolAnswerList,
	    boolean random, LinkedList<String> linkList,
	    LinkedList<String> videoList, LinkedList<String> audioList,
	    LinkedList<String> imageList, int doAction);

    /**
     * Einfuegen eines neuen Knotens.
     * 
     * @param questionText
     *            Der Fragetext.
     * @param points
     *            Die Punkte.
     * @param answerList
     *            Die Liste der Antworttexte.
     * @param boolAnswerList
     *            Die korrekt-Liste der Antworten.
     * @param random
     *            Indikator fuer Randomisierung der Antworten.
     * @param linkList
     *            Die Link-Liste.
     * @param videoList
     *            Die Video-Liste.
     * @param audioList
     *            Die Audio-Liste.
     * @param imageList
     *            Die Image-Liste.
     */
    void addNewNode(String questionText, int points,
	    LinkedList<String> answerList, LinkedList<Boolean> boolAnswerList,
	    boolean random, LinkedList<String> linkList,
	    LinkedList<String> videoList, LinkedList<String> audioList,
	    LinkedList<String> imageList);

    /**
     * Laden des naechsten Knotens. Der aktuelle wird zuvor gespeichert.
     * 
     * @param questionText
     *            Der Fragetext.
     * @param points
     *            Die Punkte.
     * @param answerList
     *            Die Liste der Antworttexte.
     * @param boolAnswerList
     *            Die korrekt-Liste der Antworten.
     * @param random
     *            Indikator fuer Randomisierung der Antworten.
     * @param linkList
     *            Die Link-Liste.
     * @param videoList
     *            Die Video-Liste.
     * @param audioList
     *            Die Audio-Liste.
     * @param imageList
     *            Die Image-Liste.
     */
    void showNodeNext(String questionText, int points,
	    LinkedList<String> answerList, LinkedList<Boolean> boolAnswerList,
	    boolean random, LinkedList<String> linkList,
	    LinkedList<String> videoList, LinkedList<String> audioList,
	    LinkedList<String> imageList);

    /**
     * Laden des vorherigen Knotens. Der aktuelle wird zuvor gespeichert.
     * 
     * @param questionText
     *            Der Fragetext.
     * @param points
     *            Die Punkte.
     * @param answerList
     *            Die Liste der Antworttexte.
     * @param boolAnswerList
     *            Die korrekt-Liste der Antworten.
     * @param random
     *            Indikator fuer Randomisierung der Antworten.
     * @param linkList
     *            Die Link-Liste.
     * @param videoList
     *            Die Video-Liste.
     * @param audioList
     *            Die Audio-Liste.
     * @param imageList
     *            Die Image-Liste.
     */
    void showNodeBefore(String questionText, int points,
	    LinkedList<String> answerList, LinkedList<Boolean> boolAnswerList,
	    boolean random, LinkedList<String> linkList,
	    LinkedList<String> videoList, LinkedList<String> audioList,
	    LinkedList<String> imageList);

    /**
     * Speichern des aktuellen Knotens und beenen der Testgenerierung.
     * 
     * @param questionText
     *            Der Fragetext.
     * @param points
     *            Die Punkte.
     * @param answerList
     *            Die Liste der Antworttexte.
     * @param boolAnswerList
     *            Die korrekt-Liste der Antworten.
     * @param random
     *            Indikator fuer Randomisierung der Antworten.
     * @param linkList
     *            Die Link-Liste.
     * @param videoList
     *            Die Video-Liste.
     * @param audioList
     *            Die Audio-Liste.
     * @param imageList
     *            Die Image-Liste.
     */
    void saveNexit(String questionText, int points,
	    LinkedList<String> answerList, LinkedList<Boolean> boolAnswerList,
	    boolean random, LinkedList<String> linkList,
	    LinkedList<String> videoList, LinkedList<String> audioList,
	    LinkedList<String> imageList);

    /**
     * Loeschen einer (Media-)ZusatzInfo.
     * 
     * @param id
     *            Die ZusatzInfo-ID.
     */
    void deleteMedia(int id);

    /**
     * Uebergibt den aktuellen Knoten.
     * 
     * @return Der aktuelle Knoten.
     */
    Node getCurrentNode();

    /**
     * Uebergibt die aktuelle Frage.
     * 
     * @return Die aktuelle Frage.
     */
    Question getCurrentQuestion();

    /**
     * Uebergibt die Liste der aktuellen Antworten.
     * 
     * @return Die aktuelle Antwortliste.
     */
    LinkedList<Answer> getCurrentAnswerList();

    /**
     * Uebergibt die Liste der aktuellen ZusatzInfo-Links.
     * 
     * @return Die Liste der aktuellen ZusatzInfo-Links.
     */
    LinkedList<AdditionalInfo> getCurrentLinkList();

    /**
     * Uebergibt die Liste der aktuellen ZusatzInfo-Videos.
     * 
     * @return Die Liste der aktuellen ZusatzInfo-Videos.
     */
    LinkedList<AdditionalInfo> getCurrentVideoList();

    /**
     * Uebergibt die Liste der aktuellen ZusatzInfo-Audios.
     * 
     * @return Die Liste der aktuellen ZusatzInfo-Audios.
     */
    LinkedList<AdditionalInfo> getCurrentAudioList();

    /**
     * Uebergibt die Liste der aktuellen ZusatzInfo-Images.
     * 
     * @return Die Liste der aktuellen ZusatzInfo-Images.
     */
    LinkedList<AdditionalInfo> getCurrentImageList();

    /**
     * Testest, ob eine Kante von Knoten zu Knoten existiert und gibt
     * entsprechenden Lookback und Bedingungs zurueck.
     * 
     * @param positionNode1
     *            Position des Quellknotens in der Knotenliste.
     * @param positionNode2
     *            Position des Zielknotens in der Knotenliste.
     * @return falls Kante existiert, Zweielementige Liste (Lookback, Punkte);
     *         sonst Liste der Groesse 0.
     */
    LinkedList<String> checkForExistingEdge(int positionNode1, int positionNode2);

    /**
     * Setzt den Indikator fuer zufaellige Verteilung der Antwortoptionen.
     * 
     * @param randomAnswers
     *            true, für aktiviert, false sonst.
     */
    void setRandomAnswers(boolean randomAnswers);

    /**
     * Übergibr den Indikator fuer zufaellige Verteilung der Antwortoptionen.
     * 
     * @return true, für aktiviert, fals sonst.
     */
    boolean isRandomAnswers();
    
    
}
