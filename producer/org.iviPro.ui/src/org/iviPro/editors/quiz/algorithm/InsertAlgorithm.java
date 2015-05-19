package org.iviPro.editors.quiz.algorithm;

import java.util.LinkedList;

import org.iviPro.model.quiz.Condition;
import org.iviPro.model.quiz.Edge;
import org.iviPro.model.quiz.Node;

/**
 * Interface fuer Implementierung eines Knoten-Einfuege-Algorithmus.
 * 
 * @author Sabine Gattermann
 * 
 */
public interface InsertAlgorithm {

    /**
     * Getter fuer Algorithmus-Typ.
     * 
     * @return 0, falls linear, 1 falls dynamisch.
     */
    int getType();

    /**
     * Getter fuer PunkteListe, die dem User in der View zur Auswahl stehen
     * soll. (Bezieht sich auf maxPunkte)
     * 
     * @return Die Punkteliste.
     */
    String[] getPointsList();

    /**
     * Einfuegen eines Knotens
     */
    void insertNode();

    /**
     * Loeschen eines Knotens.
     */
    void deleteNode();

    /**
     * Einfuegen einer Kante.
     * 
     * @param positionSourceInNodeList
     *            Die Position des Quellknotens in der Knotenliste.
     * @param positionDestinationInNodeList
     *            Die Position des Zielknotens in der Knotenliste.
     * @param conditionLookback
     *            Der Lookback.
     * @param conditionPoints
     *            Die Bedingung (als Punkte).
     */
    void insertEdge(int positionSourceInNodeList,
	    int positionDestinationInNodeList, int conditionLookback,
	    int conditionPoints);

    /**
     * Loeschen einer Kante.
     * 
     * @param positionSourceInNodeList
     *            Die Position des Quellknotens in der Knotenliste.
     * @param positionDestinationInNodeList
     *            Die Position des Zielknotens in der Knotenliste.
     * 
     */
    void deleteEdge(int positionSourceInNodeList,
	    int positionDestinationInNodeList);

    /**
     * Getter fuer DummyBedingung.
     * 
     * @return Die DummyBedingung.
     */
    Condition getDummyCondition();

    /**
     * Getter fuer PunkteListe der Bedingung, die dem User in der View zur
     * Auswahl stehen soll.
     * 
     * @param positionNode1
     *            Position des Quellknotens in der Knotenliste.
     * @param positionNode2
     *            Position des Zielknotens in der Knotenliste.
     * @param lookback
     *            Der Lookback.
     * @return Das Punkte-Array.
     */
    String[] getPossiblePoints(int positionNode1, int positionNode2,
	    int lookback);

    /**
     * Getter fuer LookbackListe der Bedingung, die dem User in der View zur
     * Auswahl stehen soll.
     * 
     * @param nodeNumber
     *            Die Knotennummer.
     * @return Das Lookback-Array.
     */
    String[] getPossibleLookback(int nodeNumber);

    /**
     * Setter fuer Lookback.
     * 
     * @param lookback
     *            Der Lookback.
     */
    void setLookback(int lookback);

    /**
     * Getter fuer PunkteListe der Bedingung.
     * 
     * @return Das Bedingungs-Punkte-Array.
     */
    String[] getConditionPointsForField();

    /**
     * Veranlasst eine interne Neuberechnung von Bedingungspunkten und Lookback.
     */
    void calculateEdge();

    /**
     * Uebergibt an das Model, was der Benutzer im Zest-Graphen angeklickt hat.
     * 
     * @param from
     *            Die Position des Quellknotens in der (Model-)KnotenListe.
     * @param to
     *            Die Position des Zielknotens in der (Model-)KnotenListe.
     * @param k
     *            Die Kante.
     */
    void setEdgeNumbersForGui(int from, int to, Edge k);

    /**
     * Getter zum Fuellen der Frage-von-zu-Combos der Edge-View.
     * 
     * @return Die Liste.
     */
    LinkedList<String> getEdgeNumbersForGui();

    /**
     * Getter zum Fuellen der Lookback-Combo der Edge-View.
     * 
     * @param node
     *            Der Knoten.
     * 
     * @return Die Liste.
     */
    String[] getConditionLookbackForField(Node node);

    /**
     * Fuehrt ein DB-Update der aktuellen Bedingung durch.
     */
    void updateEdgeCondition();

    /**
     * Fuegt in der DB eine neue Kante ein, falls zwischen den beiden Knoten
     * bereits eine Kante besteht, wird ein Update durchgefuehrt.
     * 
     * @param positionSourceInNodeList
     *            Die Position des Quellknotens in der (Model-)Knotenliste.
     * @param positionDestinationInNodeList
     *            Die Position des Zielknotens in der (Model-)Knotenliste.
     * @param conditionPoints
     *            Die Bedingungs-Punkte.
     * @param conditionLookback
     *            Der Lookback.
     */
    void updateEdge(int positionSourceInNodeList,
	    int positionDestinationInNodeList, int conditionPoints,
	    int conditionLookback);

    /**
     * Ueberprueft, ob unterhalb des Knotens mit uebergebener ID eine weitere
     * Kante eingefuegt werden kann.
     * 
     * @param id
     *            Die Knoten-ID.
     * @return true, falls Kante eingefuegt werden kann, false sonst.
     */
    boolean canInsertAnotherEdge(int id);

}
