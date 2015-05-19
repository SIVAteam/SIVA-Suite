package org.iviPro.editors.quiz.interfaces;

import java.util.LinkedList;

import org.iviPro.model.quiz.Test;

/**
 * Interface für Kommunikation zwischen Model und View.
 * 
 * @author Sabine Gattermann
 * 
 */
public interface TestManagementInterface {
    /**
     * Oeffnet den Test mit angegebenen Titel.
     * 
     * @param title
     *            Der Titel.
     */
    Test loadTestWithTitel(int id);

    /**
     * Exportiert den gewaehlten Test (in den Ordner 'Exportierte Tests')
     * 
     * @param path
     *            Der Pfad zum Ordner.
     * 
     * @param title
     *            Der Titel.
     * @param newTitleForExport
     *            Der alternative Export-Titel.
     */
    void exportTest(String path, String title, String newTitleForExport);

    /**
     * Getter fuer Nachrichten an den Benutzer.
     * 
     * @return Der Nachrichten-String.
     */
    String getMsg();

    /**
     * Getter fuer die Liste oeffentlicher (und eigener) Tests.
     * 
     * @return Die Testliste.
     */
    LinkedList<Test> getPoolTestList();

    /**
     * Getter fuer zu den PoolTests zugehoerigen Benutzernamen (gleiche
     * Reihenfolge).
     * 
     * @return Die Liste der Benutzernamen.
     */
    LinkedList<String> getUserNamesForPoolTestList();

    /**
     * Getter fuer Liste der eigenen Tests (oeffentlich u. privat).
     * 
     * @return Die Testliste.
     */
    LinkedList<Test> getTests();

    /**
     * Loeschen eines Tests.
     * 
     * @param id
     *            Die Test-ID.
     * @param category
     *            Die Kategorie.
     */
    void deleteTest(int id, String category);

    /**
     * Importieren (bzw. kopieren) eines Tests.
     * 
     * @param testId
     *            Die Test-ID.
     * @param testName
     *            Der Titel.
     */
    void importTest(int testId, String testName);

    /**
     * Backbutton
     */
    void cancel();

    /**
     * Schliesst die View.
     */
    void close();

    /**
     * Parameter fuer View-Steuerung.
     * 
     * @return 2, fuer privater Pool, 3, fuer oeffentlichen Pool.
     */
    int getDoAction();
}
